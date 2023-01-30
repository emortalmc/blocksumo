package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class PlayerBlockHandler {

    private final BlockSumoGame game;

    public PlayerBlockHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            event.consumeBlock(false);

            final Player player = event.getPlayer();
            final Point blockPosition = event.getBlockPosition();

            if (blockPosition.distanceSquared(player.getPosition().add(0, 1, 0)) > 5 * 5) {
                event.setCancelled(true);
                return;
            }

            if (blockPosition.y() > 77 || blockPosition.y() < 51.5) {
                event.setCancelled(true);
                return;
            }

            final PowerUp heldItem = game.getPowerUpManager().getHeldPowerUp(event.getPlayer(), event.getHand());
            if (heldItem == null) return;

            if (heldItem.shouldHandleBlockPlace()) {
                event.setCancelled(true);
                heldItem.onBlockPlace(player, event.getHand(), event.getBlockPosition().add(0.5, 0.1, 0.5));
                return;
            }
        });

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            final String blockName = event.getBlock().name().toLowerCase(Locale.ROOT);
            if (!blockName.contains("wool")) event.setCancelled(true);
        });
    }
}
