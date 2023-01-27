package dev.emortal.minestom.blocksumo.game.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract sealed class BlockSumoEvent permits TNTRainEvent, MapClearEvent {
    protected final @NotNull BlockSumoGame game;
    private final @NotNull Component startMessage;

    protected BlockSumoEvent(@NotNull BlockSumoGame game, @NotNull Component startMessage) {
        this.game = game;
        this.startMessage = startMessage;
    }

    public abstract void start();

    public @NotNull Component getStartMessage() {
        return this.startMessage;
    }
}
