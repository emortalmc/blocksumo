package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class BlockRainEvent implements BlockSumoEvent {
    private static final Block[] BLOCKS = Block.values().toArray(new Block[0]);
    private static final Component START_MESSAGE = Component.text()
            .append(Component.text("Uh oh...", NamedTextColor.RED))
            .append(Component.text(" it's raining blocks!", NamedTextColor.YELLOW))
            .build();
    private final @NotNull BlockSumoGame game;

    public BlockRainEvent(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    @Override
    public void start() {
        Instance instance = this.game.getInstance();
        instance.scheduler().submitTask(new BlockRainTask(this.game));
    }

    @Override
    public @NotNull Component getStartMessage() {
        return START_MESSAGE;
    }

    private static final class BlockRainTask implements Supplier<TaskSchedule> {

        private final @NotNull BlockSumoGame game;

        private int i = 0;

        BlockRainTask(@NotNull BlockSumoGame game) {
            this.game = game;
        }

        @Override
        public @NotNull TaskSchedule get() {
            if (this.i >= 16) return TaskSchedule.stop();

            for (int ii = 0; ii<8; ii++) {
                Entity entity = new BlockRainEntity();
                Pos randomPos = new Pos(
                        ThreadLocalRandom.current().nextDouble(-15, 15),
                        110,
                        ThreadLocalRandom.current().nextDouble(-15, 15)
                );
                entity.setInstance(game.getInstance(), randomPos);
            }

            this.i++;
            return TaskSchedule.tick(10);
        }
    }

    private static final class BlockRainEntity extends Entity {

        private final Block block;
        public BlockRainEntity() {
            super(EntityType.FALLING_BLOCK);

            block = BLOCKS[ThreadLocalRandom.current().nextInt(BLOCKS.length)];

            editEntityMeta(FallingBlockMeta.class, meta -> {
                meta.setBlock(block);
            });
        }

        @Override
        public void tick(long time) {
            super.tick(time);
            if (isOnGround()) {
                getInstance().setBlock(getPosition(), block);
                remove();
            }
        }
    }

}
