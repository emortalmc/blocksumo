package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PowerUpManager {
    private final PowerUpRegistry registry;
    private final BlockSumoGame game;

    public PowerUpManager(@NotNull BlockSumoGame game) {
        this.registry = new PowerUpRegistry();
        this.game = game;
    }

    public @Nullable PowerUp getHeldPowerUp(@NotNull Player player, @NotNull Player.Hand hand) {
        final ItemStack heldItem = player.getItemInHand(hand);
        final String powerUpId = getPowerUpId(heldItem);
        return registry.findById(powerUpId);
    }

    private @NotNull String getPowerUpId(@NotNull ItemStack powerUpItem) {
        return powerUpItem.getTag(PowerUp.ID);
    }
}
