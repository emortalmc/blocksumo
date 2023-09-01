package dev.emortal.minestom.blocksumo.map;

import com.google.gson.*;
import net.minestom.server.coordinate.Pos;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public record MapData(String name, int time, Set<Pos> spawns) {
    public static final Pos CENTER = new Pos(0.5, 65, 0.5);

    // Gson parser
    public static class Adapter implements JsonDeserializer<MapData> {

        @Override
        public MapData deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();

            String name = json.get("name").getAsString();
            int time = json.get("time").getAsInt();
            int spawnRadius = json.get("spawnRadius").getAsInt();

            return new MapData(name, time, this.createSpawns(spawnRadius));
        }

        private Set<Pos> createSpawns(int spawnRadius) {
            Set<Pos> spawns = new HashSet<>();
            // radius is of a circle
            Pos previousPos = null;
            for (double i = 0; i <= 2 * Math.PI; i += 0.01) {
                double c1 = spawnRadius * Math.cos(i);
                double c2 = spawnRadius * Math.sin(i);

                Pos pos = new Pos(CENTER.x() + c1, CENTER.y(), CENTER.z() + c2);
                Pos blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
                pos = blockPos.add(0.5, 0, 0.5)
                        .withLookAt(CENTER)
                        .withPitch(0);

                if (pos.equals(previousPos)) continue;

                spawns.add(pos);
                previousPos = pos;
            }

            return spawns;
        }
    }
}
