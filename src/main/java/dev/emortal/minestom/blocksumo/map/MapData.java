package dev.emortal.minestom.blocksumo.map;

import com.google.gson.*;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public record MapData(@NotNull String name, int time, int spawnRadius, @NotNull List<String> credits) {
    public static final @NotNull Pos CENTER = new Pos(0.5, 65, 0.5);

    // Gson parser
    public static class Adapter implements JsonDeserializer<MapData> {

        @Override
        public MapData deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();

            String name = json.get("name").getAsString();
            int time = json.get("time").getAsInt();
            int spawnRadius = json.get("spawnRadius").getAsInt();
            List<String> credits = context.deserialize(json.get("credits"), List.class);

            return new MapData(name, time, spawnRadius, credits);
        }

    }
}
