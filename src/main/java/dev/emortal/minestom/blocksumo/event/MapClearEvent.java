package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;

import java.util.function.Supplier;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.Batch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class MapClearEvent extends BlockSumoEvent {
    private static final Component START_MESSAGE = Component.text()
            .append(Component.text("Uh oh...", NamedTextColor.RED))
            .append(Component.text(" the map is being cleared", NamedTextColor.YELLOW))
            .append(Component.text("!", NamedTextColor.GRAY))
            .build();
    private static final int MAP_SIZE = 19;
    private static final int MAX_CLEAR_HEIGHT = 80;
    private static final int DIAMOND_BLOCK_HEIGHT = 63;

    public MapClearEvent(@NotNull BlockSumoGame game) {
        super(game, START_MESSAGE);
    }

    @Override
    public void start() {
        final BlockSumoInstance instance = game.getInstance();
        instance.scheduler().submitTask(new Supplier<>() {
            final int iterations = MAX_CLEAR_HEIGHT - DIAMOND_BLOCK_HEIGHT;
            int i = 0;

            @Override
            public TaskSchedule get() {
                if (i >= iterations) return TaskSchedule.stop();

                final AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
                removeBlocksInRange(batch, i);
                batch.apply(instance, () -> {});

                playClearLayerSound();
                i++;
                return TaskSchedule.millis(150);
            }
        });
    }

    private <C> void removeBlocksInRange(@NotNull Batch<C> batch, int currentIteration) {
        for (int x = -MAP_SIZE; x <= MAP_SIZE; x++) {
            for (int z = -MAP_SIZE; z <= MAP_SIZE; z++) {
                batch.setBlock(x, 81 - currentIteration, z, Block.AIR);
                batch.setBlock(x, 82 - currentIteration, z, Block.AIR);
                batch.setBlock(x, 83 - currentIteration, z, Block.AIR);
            }
        }
    }

    private void playClearLayerSound() {
        game.getAudience().playSound(
                Sound.sound(SoundEvent.ENTITY_EGG_THROW, Sound.Source.BLOCK, 1F, 0.5F),
                Sound.Emitter.self()
        );
    }
}
