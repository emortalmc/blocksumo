package dev.emortal.minestom.blocksumo.utils.raycast;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RaycastResult(@NotNull RaycastResultType type, @Nullable Entity hitEntity, @Nullable Point hitPosition) {
}
