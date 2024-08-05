package dev.emortal.minestom.blocksumo.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dev.emortal.minestom.blocksumo.utils.RandomStringGenerator;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(MapData.class, new MapData.Adapter()).create();

    private static final int CHUNK_LOADING_RADIUS = 5;

    private static final DimensionType DIMENSION_TYPE = DimensionType.builder()
            .hasSkylight(true)
            .build();
    private static final DimensionType FULLBRIGHT_DIMENSION_TYPE = DimensionType.builder()
            .ambientLight(1f)
            .build();

    private static final List<String> ENABLED_MAPS = List.of(
            "blocksumo",
            "castle",
            "end",
            "ice",
            "ruins"
//            "deepdark"
    );
    private static final Path MAPS_PATH = Path.of("maps");

    private final @NotNull Map<String, PreLoadedMap> preLoadedMaps;

    public MapManager() {
        MinecraftServer.getDimensionTypeRegistry().register("emortalmc:blocksumo", DIMENSION_TYPE);
        MinecraftServer.getDimensionTypeRegistry().register("emortalmc:blocksumofb", FULLBRIGHT_DIMENSION_TYPE);

        Map<String, PreLoadedMap> maps = new HashMap<>();
        for (String mapName : ENABLED_MAPS) {
            Path mapPath = MAPS_PATH.resolve(mapName);
            Path polarPath = mapPath.resolve("map.polar");
            Path dataPath = mapPath.resolve("map_data.json");

            try {
                MapData mapData = GSON.fromJson(new JsonReader(new FileReader(dataPath.toFile())), MapData.class);
                LOGGER.info("Loaded map data for map {}: [{}]", mapName, mapData);

                PolarLoader polarLoader = new PolarLoader(polarPath);
//                if (!Files.exists(polarPath)) { // File needs to be converted
//                    PolarWorld world = AnvilPolar.anvilToPolar(mapPath, ChunkSelector.radius(CHUNK_LOADING_RADIUS));
//                    Files.write(polarPath, PolarWriter.write(world));
//                    polarLoader = new PolarLoader(world);
//                } else {
//                    polarLoader = new PolarLoader(polarPath);
//                }

                maps.put(mapName, new PreLoadedMap(polarLoader, mapData));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }

        this.preLoadedMaps = Map.copyOf(maps);
    }

    public @NotNull LoadedMap getMap(@Nullable String id) {
        if (id == null) {
            return this.getRandomMap();
        }

        PreLoadedMap map = this.preLoadedMaps.get(id);
        if (map == null) {
            LOGGER.warn("Map {} not found, loading random map", id);
            return this.getRandomMap();
        }

        return map.load();
    }

    public @Nullable MapData getMapData(@NotNull String id) {
        PreLoadedMap map = this.preLoadedMaps.get(id);
        if (map == null) {
            LOGGER.warn("Map {} not ", id);
            return null;
        }

        return map.getMapData();
    }

    public @NotNull LoadedMap getRandomMap() {
        String randomMapId = ENABLED_MAPS.get(ThreadLocalRandom.current().nextInt(ENABLED_MAPS.size()));

        PreLoadedMap map = this.preLoadedMaps.get(randomMapId);
        return map.load();
    }

    private class PreLoadedMap {

        private final PolarLoader chunkLoader;
        private final MapData mapData;
        public PreLoadedMap(@NotNull PolarLoader chunkLoader, @NotNull MapData mapData) {
            this.chunkLoader = chunkLoader;
            this.mapData = mapData;
        }

        @NotNull LoadedMap load() {
            DynamicRegistry<DimensionType> dimRegistry = MinecraftServer.getDimensionTypeRegistry();
            DynamicRegistry.Key<DimensionType> dimensionType = dimRegistry.getKey(DIMENSION_TYPE);
            InstanceContainer newInstance = MinecraftServer.getInstanceManager().createInstanceContainer(dimensionType);

            newInstance.setChunkLoader(this.chunkLoader);
            newInstance.enableAutoChunkLoad(false);

            for (int x = -CHUNK_LOADING_RADIUS; x < CHUNK_LOADING_RADIUS; x++) {
                for (int z = -CHUNK_LOADING_RADIUS; z < CHUNK_LOADING_RADIUS; z++) {
                    newInstance.loadChunk(x, z);
                }
            }

            // Probably won't save any memory by removing chunk loader because PreLoadedMap needs to hold a reference anyway

            return new LoadedMap(newInstance, this.mapData);
        }

        public MapData getMapData() {
            return mapData;
        }

        private static @NotNull NamespaceID generateDimensionId() {
            String randomValue = RandomStringGenerator.generate(16);
            return NamespaceID.from("emortalmc", "dim_" + randomValue);
        }
    }
}