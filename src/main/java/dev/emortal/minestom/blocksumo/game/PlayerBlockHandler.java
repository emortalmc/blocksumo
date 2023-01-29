package dev.emortal.minestom.blocksumo.game;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerBlockHandler {

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            event.consumeBlock(false);
        });
    }
}
