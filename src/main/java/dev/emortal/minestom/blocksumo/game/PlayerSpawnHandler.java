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
    private final int playerCount;
    private int playerIndex = 0;

    public PlayerSpawnHandler(@NotNull BlockSumoGame game, @NotNull List<Pos> availableSpawns, int playerCount) {
        this.game = game;
        this.availableSpawns = availableSpawns;
        this.playerCount = playerCount;
    }

    public @NotNull Pos getCircleSpawn() {
        if (playerCount == 0) { // probably during testing
            return availableSpawns.get(playerIndex++);
        }

        double spawnsPerIndex = (double) availableSpawns.size() / playerCount;

        return availableSpawns.get((int) Math.floor(playerIndex++ * spawnsPerIndex));
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
