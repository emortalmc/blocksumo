package dev.emortal.minestom.blocksumo.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class EventRegistry {

    private final Map<String, EventConstructor> registry = new HashMap<>();

    public @Nullable EventConstructor findByName(final @NotNull String eventName) {
        return registry.get(eventName);
    }

    public @NotNull EventConstructor findRandom() {
        final EventConstructor[] values = registry.values().toArray(new EventConstructor[0]);
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    public void registerEvent(final @NotNull String name, final @NotNull EventConstructor constructor) {
        if (registry.containsKey(name)) {
            throw new IllegalArgumentException("Event with name " + name + " already exists!");
        }
        registry.put(name, constructor);
    }

    public @NotNull Collection<String> getEventNames() {
        return registry.keySet();
    }
}
