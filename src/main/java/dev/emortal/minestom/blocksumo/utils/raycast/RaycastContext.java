package dev.emortal.minestom.blocksumo.utils.raycast;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public record RaycastContext(@NotNull Instance instance, @NotNull Point start, @NotNull Vec direction, double maxDistance,
                             @NotNull EntityHitPredicate entityHitPredicate) {
}
