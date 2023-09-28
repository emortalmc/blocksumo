package dev.emortal.minestom.blocksumo.spawning;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapData;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

final class RespawnPointSelector {
    private static final double TWO_PI = Math.PI * 2;
    private static final double CHECK_OFFSET = TWO_PI / 150;

    private final @NotNull BlockSumoGame game;
    private final int spawnRadius;

    RespawnPointSelector(@NotNull BlockSumoGame game, int spawnRadius) {
        this.game = game;
        this.spawnRadius = spawnRadius;
    }

    public @NotNull Pos select() {
        double distanceHighscore = 0;
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
        Pos direction = MapData.CENTER.sub(pos.x(), 0, pos.z());

        pos = pos.withDirection(direction);

        return MapData.CENTER.add(pos);
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
        return distance;
    }
}
