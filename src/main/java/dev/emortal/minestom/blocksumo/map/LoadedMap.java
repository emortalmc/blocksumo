package dev.emortal.minestom.blocksumo.map;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public record LoadedMap(@NotNull Instance instance, @NotNull MapData data) {
}
