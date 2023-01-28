package dev.emortal.minestom.blocksumo.powerup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class PowerUpRegistry {
    private final Map<String, PowerUpConstructor> registry = new HashMap<>();

    public @Nullable PowerUpConstructor findById(final @NotNull String id) {
        return registry.get(id);
    }

    public void registerPowerUp(final @NotNull String id, final @NotNull PowerUpConstructor constructor) {
        if (registry.containsKey(id)) {
            throw new IllegalArgumentException("PowerUp with id " + id + " already exists!");
        }
        registry.put(id, constructor);
    }

    public @NotNull Collection<String> getPowerUpIds() {
        return registry.keySet();
    }
}
