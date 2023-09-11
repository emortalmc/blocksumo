package dev.emortal.minestom.blocksumo.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class EventRegistry {

    private final Map<String, EventConstructor> registry = new HashMap<>();

    public @Nullable EventConstructor findByName(@NotNull String eventName) {
        return this.registry.get(eventName);
    }

    public @NotNull EventConstructor findRandom() {
        EventConstructor[] values = this.registry.values().toArray(new EventConstructor[0]);
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    public void registerEvent(@NotNull String name, @NotNull EventConstructor constructor) {
        if (this.registry.containsKey(name)) {
            throw new IllegalArgumentException("Event with name " + name + " already exists!");
        }
        this.registry.put(name, constructor);
    }

    public @NotNull Collection<String> getEventNames() {
        return this.registry.keySet();
    }
}
