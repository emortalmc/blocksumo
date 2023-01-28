package dev.emortal.minestom.blocksumo.powerup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerUpRegistry {
    private final Map<String, PowerUp> registry = new HashMap<>();

    public @Nullable PowerUp findByName(final @NotNull String name) {
        return registry.get(name);
    }

    public @NotNull PowerUp findRandom() {
        final PowerUp[] values = registry.values().toArray(new PowerUp[0]);
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    public void registerPowerUp(final @NotNull PowerUp powerUp) {
        final String name = powerUp.getName();
        if (registry.containsKey(name)) {
            throw new IllegalArgumentException("Power up with name " + name + " already exists!");
        }
        registry.put(name, powerUp);
    }

    public @NotNull Collection<String> getPowerUpNames() {
        return registry.keySet();
    }
}
