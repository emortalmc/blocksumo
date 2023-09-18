package dev.emortal.minestom.blocksumo.spawning;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InitialSpawnPointSelector {

    private final @NotNull List<Pos> availableSpawns;

    private final Set<Pos> usedSpawns = new HashSet<>();

    public InitialSpawnPointSelector(@NotNull List<Pos> availableSpawns) {
        this.availableSpawns = availableSpawns;
    }

    // We synchronize this to avoid all issues with multiple players joining at around the same time
    public synchronized @NotNull Pos select() {
        if (this.usedSpawns.isEmpty()) {
            return this.firstPos();
        }
        if (this.usedSpawns.size() == 1) {
            return this.secondPos();
        }

        Pos bestPos = this.availableSpawns.get(0);
        double distanceHighscore = Double.MIN_VALUE; // min value so total distance is above highscore on first iteration

        for (Pos spawnPos : this.availableSpawns) {
            double totalDistance = 0.0;

            for (Pos used : this.usedSpawns) {
                totalDistance += used.distanceSquared(spawnPos);
            }

            if (totalDistance > distanceHighscore) {
                distanceHighscore = totalDistance;
                bestPos = spawnPos;
            }
        }

        return this.useSpawn(bestPos);
    }

    private @NotNull Pos firstPos() {
        // For the first pos we always get the first spawn
        return this.useSpawn(this.availableSpawns.get(0));
    }

    private @NotNull Pos secondPos() {
        // For the second pos we always get the one in the middle of the list, which will always be opposite the first pos
        int index = this.availableSpawns.size() / 2;
        return this.useSpawn(this.availableSpawns.get(index));
    }

    private @NotNull Pos useSpawn(@NotNull Pos pos) {
        this.usedSpawns.add(pos);
        return pos;
    }
}
