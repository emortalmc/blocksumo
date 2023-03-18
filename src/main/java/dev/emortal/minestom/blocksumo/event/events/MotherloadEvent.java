package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MotherloadEvent extends BlockSumoEvent {
    private static final Component START_MESSAGE = MiniMessage.miniMessage()
            .deserialize("<red>☢ ‼</red> <yellow>MOTHERLOAD INCOMING</yellow> <red>‼ ☢</red>");

    private static final Pos DIAMOND_BLOCK_POS = new Pos(0.5, 64, 0.5);
    private static final Pos MOTHERLOAD_SPAWN_POS = DIAMOND_BLOCK_POS.add(0, 50, 0);

    public MotherloadEvent(@NotNull BlockSumoGame game) {
        super(game, START_MESSAGE);
    }

    @Override
    public void start() {
        new MotherloadInstance(this.game);
    }

    private static class MotherloadInstance {
        private final @NotNull Entity minecart;

        public MotherloadInstance(@NotNull BlockSumoGame game) {
            this.minecart = new MotherloadCart(game);
        }
    }

    private static class MotherloadCart extends Entity {
        private static final TaskSchedule SPAWN_DELAY = TaskSchedule.duration(15, TimeUnit.CLIENT_TICK);

        private final AtomicBoolean detonating = new AtomicBoolean(false);
        private final @NotNull BlockSumoGame game;

        public MotherloadCart(@NotNull BlockSumoGame game) {
            super(EntityType.TNT_MINECART);
            this.game = game;

            this.setGravity(0, this.getGravityAcceleration() / 3.5);

            MinecraftServer.getSchedulerManager().buildTask(() -> {
                this.setInstance(game.getInstance(), MOTHERLOAD_SPAWN_POS);
            }).delay(SPAWN_DELAY).schedule();
        }

        @Override
        public void tick(long time) {
            super.tick(time);

            if (this.position.distanceSquared(DIAMOND_BLOCK_POS) <= 1.5 && this.detonating.compareAndSet(false, true)) {
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    this.remove();
                    this.detonate();
                }).delay(5, TimeUnit.CLIENT_TICK).schedule();
            }
        }

        private void detonate() {
            this.game.getExplosionManager().nuclearExplosion(this.position, this, 15);
        }
    }
}
