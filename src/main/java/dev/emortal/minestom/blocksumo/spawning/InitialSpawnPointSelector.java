package dev.emortal.minestom.blocksumo.spawning;

import dev.emortal.minestom.blocksumo.map.MapData;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;

public final class InitialSpawnPointSelector {
    private static final double TWO_PI = Math.PI * 2;

    private final Queue<Pos> spawns;

    public InitialSpawnPointSelector(int playerCount, int spawnRadius) {
        this.spawns = this.generateSpawns(playerCount, spawnRadius);
    }

    private @NotNull Queue<Pos> generateSpawns(int playerCount, int spawnRadius) {
        double playerOffset = TWO_PI / Math.max(playerCount, 1); // Fixes 0 player count on local testing

        Queue<Pos> spawns = new ArrayDeque<>();
        for (int i = 0; i <= playerCount; i++) {
            Pos pos = floorPos(new Pos(Math.cos(playerOffset * i) * spawnRadius, 0, Math.sin(playerOffset * i) * spawnRadius));

            spawns.add(MapData.CENTER.add(pos));
        }

        return spawns;
    }

    private @NotNull Pos floorPos(Pos pos) {
        return new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    public @NotNull Pos select() {
        Pos spawn = this.spawns.poll();
        if (spawn == null) return MapData.CENTER; // Happens during local testing
        return spawn;
    }

}