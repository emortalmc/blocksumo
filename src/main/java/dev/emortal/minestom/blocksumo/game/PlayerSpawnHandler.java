package dev.emortal.minestom.blocksumo.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlayerSpawnHandler {
    private final BlockSumoGame game;
    private final List<Pos> availableSpawns;
    private final Set<Pos> usedSpawns;

    public PlayerSpawnHandler(@NotNull BlockSumoGame game, @NotNull List<Pos> availableSpawns) {
        this.game = game;
        this.availableSpawns = availableSpawns;
        this.usedSpawns = new HashSet<>();
    }

    public @NotNull Pos getBestSpawn() {
        Pos bestPos = this.availableSpawns.get(0);
        double distanceHighscore = Double.MIN_VALUE; // min value so total distance is above highscore on first iteration

        for (final Pos spawnPos : this.availableSpawns) {

            double totalDistance = 0.0;

            for (final Player player : game.getPlayers()) {
                totalDistance += player.getRespawnPoint().distanceSquared(spawnPos);
            }

            if (totalDistance > distanceHighscore) {
                if (usedSpawns.contains(spawnPos)) continue;
                distanceHighscore = totalDistance;
                bestPos = spawnPos;
            }
        }

        usedSpawns.add(bestPos);
        return bestPos;
    }

    public @NotNull Pos getBestRespawn() {
        Pos bestPos = this.availableSpawns.get(0);
        double distanceHighscore = Double.MIN_VALUE; // min value so total distance is above highscore on first iteration

        for (final Pos spawnPos : this.availableSpawns) {

            double totalDistance = 0.0;

            for (final Player player : game.getPlayers()) {
                if (player.getTag(PlayerTags.DEAD)) continue;
                totalDistance += player.getPosition().distanceSquared(spawnPos);
            }

            if (totalDistance > distanceHighscore) {
                distanceHighscore = totalDistance;
                bestPos = spawnPos;
            }
        }

        return bestPos;
    }
}
