package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class PowerUpManager {
    private final PowerUpRegistry registry;
    private final BlockSumoGame game;

    public PowerUpManager(@NotNull BlockSumoGame game) {
        this.registry = new PowerUpRegistry();
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            event.setCancelled(true);

            final Player player = event.getPlayer();
            final Player.Hand hand = event.getHand();

            final PowerUp heldPowerUp = getHeldPowerUp(player, hand);
            if (heldPowerUp != null) heldPowerUp.onUse(player, hand);
        });

        eventNode.addListener(PlayerUseItemOnBlockEvent.class, event -> {
            final Player player = event.getPlayer();
            final Player.Hand hand = event.getHand();

            final PowerUp heldPowerUp = getHeldPowerUp(player, hand);
            if (heldPowerUp != null) heldPowerUp.onUseOnBlock(player, hand);
        });
    }

    public @Nullable PowerUp findNamedPowerUp(@NotNull String id) {
        return registry.findByName(id);
    }

    public @NotNull PowerUp findRandomPowerUp() {
        return registry.findRandom();
    }

    public @Nullable PowerUp getHeldPowerUp(@NotNull Player player, @NotNull Player.Hand hand) {
        final ItemStack heldItem = player.getItemInHand(hand);
        final String powerUpId = getPowerUpName(heldItem);
        return registry.findByName(powerUpId);
    }

    private @NotNull String getPowerUpName(@NotNull ItemStack powerUpItem) {
        return powerUpItem.getTag(PowerUp.NAME);
    }

    public void givePowerUp(@NotNull Player player, @NotNull PowerUp powerUp) {
        final ItemStack item = powerUp.createItemStack();
        player.getInventory().addItemStack(item);
    }

    public @NotNull Collection<String> getPowerUpIds() {
        return registry.getPowerUpNames();
    }
}
