package dev.emortal.minestom.blocksumo.spawning;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class RespawnPointSelector {

    private final @NotNull BlockSumoGame game;
    private final @NotNull List<Pos> availableSpawns;

    private final @NotNull Set<Pos> allocatedSpawns = Collections.synchronizedSet(new HashSet<>());

    RespawnPointSelector(@NotNull BlockSumoGame game, @NotNull List<Pos> availableSpawns) {
        this.game = game;
        this.availableSpawns = availableSpawns;
    }

    public @NotNull Pos select() {
        Pos bestPos = this.availableSpawns.get(0);
        double distanceHighscore = Double.MIN_VALUE; // min value so total distance is above highscore on first iteration

        for (Pos spawnPos : this.availableSpawns) {
            double totalDistance = 0.0;

            for (Player player : this.game.getPlayers()) {
                if (player.getTag(PlayerTags.DEAD)) continue;
                totalDistance += player.getPosition().distanceSquared(spawnPos);
            }

            for (Pos allocatedSpawn : this.allocatedSpawns) {
                totalDistance += allocatedSpawn.distanceSquared(spawnPos);
            }

            if (totalDistance > distanceHighscore) {
                distanceHighscore = totalDistance;
                bestPos = spawnPos;
            }
        }

        this.allocatedSpawns.add(bestPos);
        Pos finalBestPos = bestPos;
        this.game.getSpawningInstance().scheduler()
                .buildTask(() -> this.allocatedSpawns.remove(finalBestPos))
                .delay(TaskSchedule.tick(MinecraftServer.TICK_PER_SECOND * 6))
                .schedule();

        return bestPos;
    }
}
