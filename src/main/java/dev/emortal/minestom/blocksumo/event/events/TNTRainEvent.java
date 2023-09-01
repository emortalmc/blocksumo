package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.explosion.ExplosionData;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class TNTRainEvent extends BlockSumoEvent {
    private static final Component START_MESSAGE = MiniMessage.miniMessage()
            .deserialize("<red>Uh oh... <gray>prepare for <italic>lots</italic> of explosions; <yellow>the TNT rain event just started");
    private static final ExplosionData EXPLOSION = new ExplosionData(3, 33, 5, true);

    public TNTRainEvent(@NotNull BlockSumoGame game) {
        super(game, START_MESSAGE);
    }

    @Override
    public void start() {
        final Instance instance = game.getSpawningInstance();
        instance.scheduler().submitTask(new Supplier<>() {
            int i = 0;

            @Override
            public TaskSchedule get() {
                if (i >= 4) return TaskSchedule.stop();

                for (final Player player : game.getPlayers()) {
                    if (player.getGameMode() != GameMode.SURVIVAL) continue;

                    final Point tntPos = player.getPosition().add(0, 10, 0);
                    game.getExplosionManager().spawnTnt(tntPos, 80, EXPLOSION, null);
                }

                i++;
                return TaskSchedule.seconds(2);
            }
        });
    }
}
