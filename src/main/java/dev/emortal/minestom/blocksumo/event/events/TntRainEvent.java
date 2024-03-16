package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.explosion.ExplosionData;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class TntRainEvent implements BlockSumoEvent {
    private static final Component START_MESSAGE = MiniMessage.miniMessage()
            .deserialize("<red>Uh oh... <gray>prepare for <i>lots</i> of explosions; <yellow>the TNT rain event just started");
    private static final ExplosionData EXPLOSION = new ExplosionData(3, 33, 5, true);

    private final @NotNull BlockSumoGame game;

    public TntRainEvent(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    @Override
    public void start() {
        this.game.getInstance().scheduler().submitTask(new TntRainTask(this.game));
    }

    @Override
    public @NotNull Component getStartMessage() {
        return START_MESSAGE;
    }

    private static final class TntRainTask implements Supplier<TaskSchedule> {

        private final @NotNull BlockSumoGame game;

        private int i = 0;

        TntRainTask(@NotNull BlockSumoGame game) {
            this.game = game;
        }

        @Override
        public @NotNull TaskSchedule get() {
            if (this.i >= 4) return TaskSchedule.stop();

            for (Player player : this.game.getPlayers()) {
                if (player.getGameMode() != GameMode.SURVIVAL) continue;

                Point tntPos = player.getPosition().add(0, 10, 0);
                this.game.getExplosionManager().spawnTnt(tntPos, 80, EXPLOSION, null);
            }

            this.i++;
            return TaskSchedule.seconds(2);
        }
    }
}
