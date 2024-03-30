package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class MapClearEvent implements BlockSumoEvent {
    private static final Component START_MESSAGE = Component.text()
            .append(Component.text("Uh oh...", NamedTextColor.RED))
            .append(Component.text(" the map is being cleared!", NamedTextColor.YELLOW))
            .build();
    private static final int MAP_SIZE = 19;
    private static final int MAX_CLEAR_HEIGHT = 110;
    private static final int DIAMOND_BLOCK_HEIGHT = 64;

    private final @NotNull BlockSumoGame game;

    public MapClearEvent(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    @Override
    public void start() {
        Instance instance = this.game.getInstance();
        instance.scheduler().submitTask(new ClearMapTask(this.game, instance));
    }

    @Override
    public @NotNull Component getStartMessage() {
        return START_MESSAGE;
    }

    private static final class ClearMapTask implements Supplier<TaskSchedule> {
        // We must do one more iteration than the diff :)
        private static final int ITERATIONS = MAX_CLEAR_HEIGHT - DIAMOND_BLOCK_HEIGHT + 1;

        private final @NotNull BlockSumoGame game;
        private final @NotNull Instance instance;

        private int i = 0;

        ClearMapTask(@NotNull BlockSumoGame game, @NotNull Instance instance) {
            this.game = game;
            this.instance = instance;
        }

        @Override
        public @NotNull TaskSchedule get() {
            if (this.i >= ITERATIONS) return TaskSchedule.stop();

            AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
            this.removeBlocksInRange(batch, this.i);
            batch.apply(this.instance, () -> {});

            this.playClearLayerSound();
            this.i++;
            return TaskSchedule.tick(4);
        }

        private void removeBlocksInRange(@NotNull AbsoluteBlockBatch batch, int currentIteration) {
            for (int x = -MAP_SIZE; x <= MAP_SIZE; x++) {
                for (int z = -MAP_SIZE; z <= MAP_SIZE; z++) {
                    if (instance.getBlock(x, 81 - currentIteration, z, Block.Getter.Condition.TYPE).compare(Block.BARRIER)) continue;
                    batch.setBlock(x, 81 - currentIteration, z, Block.AIR);
                    batch.setBlock(x, 82 - currentIteration, z, Block.AIR);
                    batch.setBlock(x, 83 - currentIteration, z, Block.AIR);
                }
            }
        }

        private void playClearLayerSound() {
            this.game.playSound(Sound.sound(SoundEvent.ENTITY_EGG_THROW, Sound.Source.BLOCK, 1F, 0.5F), Sound.Emitter.self());
        }
    }
}
