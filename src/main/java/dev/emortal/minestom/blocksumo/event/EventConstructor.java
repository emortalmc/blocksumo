package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.event.events.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventConstructor {

    @NotNull BlockSumoEvent create(@NotNull BlockSumoGame game);
}
