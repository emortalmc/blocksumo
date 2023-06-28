package dev.emortal.minestom.blocksumo.map;

import dev.emortal.tnt.TNTLoader;
import org.jetbrains.annotations.NotNull;

public record PreLoadedMap(@NotNull TNTLoader chunkLoader, @NotNull MapData mapData) {
}
