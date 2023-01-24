package dev.emortal.minestom.blocksumo.game.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class EventRegistry {
    public static final Map<String, Function<BlockSumoGame, BlockSumoEvent>> EVENTS = Map.of(
            "TNT_RAIN", TNTRainEvent::new
    );

    public static @NotNull Function<BlockSumoGame, BlockSumoEvent> randomEvent() {
        Function<BlockSumoGame, BlockSumoEvent>[] values = EVENTS.values().toArray(new Function[0]);
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}
