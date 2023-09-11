package dev.emortal.minestom.blocksumo.powerup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerUpRegistry {

    private final Map<String, PowerUp> registry = new HashMap<>();

    public @Nullable PowerUp findByName(@NotNull String name) {
        return this.registry.get(name);
    }

    public @NotNull PowerUp findRandom() {
        PowerUp[] values = this.registry.values().toArray(new PowerUp[0]);
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    public @NotNull List<PowerUp> findAllBySpawnLocation(@NotNull SpawnLocation spawnLocation) {
        List<PowerUp> result = new ArrayList<>();
        for (PowerUp powerUp : this.registry.values()) {
            if (powerUp.getSpawnLocation() == spawnLocation) result.add(powerUp);
        }
        return result;
    }

    public void registerPowerUp(@NotNull PowerUp powerUp) {
        String name = powerUp.getName();
        if (this.registry.containsKey(name)) {
            throw new IllegalArgumentException("Power up with name " + name + " already exists!");
        }
        this.registry.put(name, powerUp);
    }

    public @NotNull Collection<String> getPowerUpNames() {
        return this.registry.keySet();
    }
}
