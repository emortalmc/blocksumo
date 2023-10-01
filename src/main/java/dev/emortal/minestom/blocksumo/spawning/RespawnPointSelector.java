package dev.emortal.minestom.blocksumo.spawning;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapData;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

final class RespawnPointSelector {
    private static final double TWO_PI = Math.PI * 2;
    private static final double CHECK_OFFSET = TWO_PI / 150;

    private final @NotNull BlockSumoGame game;
    private final int spawnRadius;
    private final @NotNull Set<Point> queuedPoints;

    RespawnPointSelector(@NotNull BlockSumoGame game, int spawnRadius) {
        this.game = game;
        this.spawnRadius = spawnRadius;
        this.queuedPoints = new HashSet<>();
    }

    public @NotNull Pos select() {
        double distanceHighscore = -1;
        Pos bestPosition = Pos.ZERO;

        for (double i = 0.0; i < TWO_PI; i += CHECK_OFFSET) {
            Pos pos = new Pos(Math.cos(i) * spawnRadius, 0, Math.sin(i) * spawnRadius);

            double distanceToPlayers = sumOfPlayerDistances(pos);
            if (distanceHighscore < distanceToPlayers) {
                distanceHighscore = distanceToPlayers;
                bestPosition = pos;
            }
        }

        Pos pos = floorPos(bestPosition);
        Pos direction = MapData.CENTER.sub(pos.x() + 0.5, MapData.CENTER.y(), pos.z() + 0.5);

        queuedPoints.add(pos);
        removeQueuedPointAfterDelay(pos);

        return MapData.CENTER.add(pos).withDirection(direction);
    }

    private @NotNull Pos floorPos(Pos pos) {
        return new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    private double sumOfPlayerDistances(Point pos) {
        double distance = 0;
        for (Player player : game.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;

            distance += player.getDistanceSquared(pos);
        }
        for (Point queuedPoint : queuedPoints) {
            distance += queuedPoint.distanceSquared(pos);
        }
        return distance;
    }

    private void removeQueuedPointAfterDelay(Pos queuedPoint) {
        game.getSpawningInstance().scheduler().buildTask(() -> queuedPoints.remove(queuedPoint))
                .delay(TaskSchedule.tick(5))
                .schedule();
    }
}
