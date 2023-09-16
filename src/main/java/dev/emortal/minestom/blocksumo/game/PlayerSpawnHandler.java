package dev.emortal.minestom.blocksumo.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlayerSpawnHandler {

    private final @NotNull BlockSumoGame game;
    private final @NotNull List<Pos> availableSpawns;

    private final @NotNull Set<Pos> allocatedSpawns = new HashSet<>();

    public PlayerSpawnHandler(@NotNull BlockSumoGame game, @NotNull List<Pos> availableSpawns) {
        this.game = game;
        this.availableSpawns = availableSpawns;
    }

    public @NotNull Pos getBestSpawn() {
        Pos bestPos = this.availableSpawns.get(0);
        double distanceHighscore = Double.MIN_VALUE; // min value so total distance is above highscore on first iteration

        for (Pos spawnPos : this.availableSpawns) {
            double totalDistance = 0.0;

            for (Player player : this.game.getPlayers()) {
                totalDistance += player.getRespawnPoint().distanceSquared(spawnPos);
            }

            if (totalDistance > distanceHighscore) {
                distanceHighscore = totalDistance;
                bestPos = spawnPos;
            }
        }

        return bestPos;
    }

    public @NotNull Pos getBestRespawn() {
        Pos bestPos = this.availableSpawns.get(0);
        double distanceHighscore = Double.MIN_VALUE; // min value so total distance is above highscore on first iteration

        for (Pos spawnPos : this.availableSpawns) {
            double totalDistance = 0.0;

            for (Player player : this.game.getPlayers()) {
                if (player.getTag(PlayerTags.DEAD)) continue;
                totalDistance += player.getPosition().distanceSquared(spawnPos);
            }
            for (Pos allocatedSpawn : allocatedSpawns) {
                totalDistance += allocatedSpawn.distanceSquared(spawnPos);
            }

            if (totalDistance > distanceHighscore) {
                distanceHighscore = totalDistance;
                bestPos = spawnPos;
            }
        }

        allocatedSpawns.add(bestPos);
        Pos finalBestPos = bestPos;
        game.getSpawningInstance().scheduler().buildTask(() -> allocatedSpawns.remove(finalBestPos))
                .delay(TaskSchedule.tick(MinecraftServer.TICK_PER_SECOND * 6))
                .schedule();

        return bestPos;
    }
}
