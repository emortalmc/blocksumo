package dev.emortal.minestom.blocksumo.game;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class PlayerBlockHandler {

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
        });

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            final String blockName = event.getBlock().name().toLowerCase(Locale.ROOT);
            if (!blockName.contains("wool")) event.setCancelled(true);
        });
    }
}
