package dev.emortal.minestom.blocksumo.map;

import net.hollowcube.polar.PolarLoader;
import org.jetbrains.annotations.NotNull;

public record PreLoadedMap(@NotNull PolarLoader chunkLoader, @NotNull MapData mapData) {
}
