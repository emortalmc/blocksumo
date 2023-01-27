package dev.emortal.minestom.blocksumo.game;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

public final class PlayerTracker {

    private final BlockSumoGame game;
    private final PlayerDeathHandler deathHandler;

    public PlayerTracker(@NotNull BlockSumoGame game, int minAllowedHeight) {
        this.game = game;
        this.deathHandler = new PlayerDeathHandler(minAllowedHeight);
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        deathHandler.registerDeathListener(eventNode);
    }

    public void addInitialTags(@NotNull Player player) {
        player.setTag(PlayerTags.LAST_DAMAGE_TIME, 0L);
    }
}
