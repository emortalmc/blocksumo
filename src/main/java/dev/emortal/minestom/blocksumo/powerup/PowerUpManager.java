package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import org.jetbrains.annotations.NotNull;

public final class PowerUpManager {
    private final PowerUpRegistry registry;
    private final BlockSumoGame game;

    public PowerUpManager(@NotNull BlockSumoGame game) {
        this.registry = new PowerUpRegistry();
        this.game = game;
    }
}
