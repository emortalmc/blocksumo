package dev.emortal.minestom.blocksumo.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dev.emortal.minestom.blocksumo.utils.gson.PosAdapter;
import dev.emortal.tnt.TNTLoader;
import dev.emortal.tnt.source.FileTNTSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class MapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MapData.class, new MapData.Adapter())
            .registerTypeAdapter(Pos.class, new PosAdapter())
            .create();

    private static final List<String> ENABLED_MAPS = List.of(
            "blocksumo",
            "castle",
            "end",
            "icebs",
            "ruinsbs"
    );
    private static final Path MAPS_PATH = Path.of("maps");

    public CompletableFuture<BlockSumoInstance> getRandomMap() {
        return CompletableFuture.supplyAsync(() -> {
            String randomMapName = ENABLED_MAPS.get(ThreadLocalRandom.current().nextInt(ENABLED_MAPS.size()));

            Path mapPath = MAPS_PATH.resolve(randomMapName);
            Path tntPath = mapPath.resolve("map.tnt");
            Path dataPath = mapPath.resolve("map_data.json");

            try {
                MapData mapData = GSON.fromJson(new JsonReader(new FileReader(dataPath.toFile())), MapData.class);
                LOGGER.info("Loaded map data for map {}: [{}]", randomMapName, mapData);

                BlockSumoInstance instance = new BlockSumoInstance(mapData, new TNTLoader(new FileTNTSource(tntPath)));
                MinecraftServer.getInstanceManager().registerInstance(instance);
                return instance;
            } catch (IOException | NBTException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
