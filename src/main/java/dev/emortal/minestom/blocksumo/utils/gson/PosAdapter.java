package dev.emortal.minestom.blocksumo.utils.gson;

import com.google.gson.*;
import net.minestom.server.coordinate.Pos;

import java.lang.reflect.Type;

public class PosAdapter implements JsonDeserializer<Pos> {

    @Override
    public Pos deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = element.getAsJsonObject();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0;
        return new Pos(x, y, z, yaw, pitch);
    }
}
