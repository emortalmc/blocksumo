package dev.emortal.minestom.blocksumo.map;

import dev.emortal.tnt.TNTLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BlockSumoInstance extends InstanceContainer {
    private final @NotNull MapData mapData;

    public BlockSumoInstance(@NotNull MapData mapData, @NotNull TNTLoader loader) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD, loader);

        this.mapData = mapData;

        this.setTimeRate(0);
        this.setTime(mapData.time());
    }

    public @NotNull MapData getMapData() {
        return this.mapData;
    }
}
