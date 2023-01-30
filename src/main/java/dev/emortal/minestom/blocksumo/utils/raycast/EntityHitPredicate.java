package dev.emortal.minestom.blocksumo.utils.raycast;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EntityHitPredicate {

    boolean test(@NotNull Entity entity);
}
