package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PowerUpConstructor {

    @NotNull PowerUp create(@NotNull BlockSumoGame game);
}
