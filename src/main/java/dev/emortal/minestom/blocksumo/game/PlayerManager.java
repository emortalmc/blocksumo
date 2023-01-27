package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerManager {

    private final BlockSumoGame game;
    private final PlayerDeathHandler deathHandler;
    private final PlayerRespawnHandler respawnHandler;

    public PlayerManager(@NotNull BlockSumoGame game, int minAllowedHeight) {
        this.game = game;
        this.deathHandler = new PlayerDeathHandler(this, minAllowedHeight);
        this.respawnHandler = new PlayerRespawnHandler(game, this);
    }

    public void registerPreGameListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            prepareInitialSpawn(player, player.getRespawnPoint());
        });
    }

    private void prepareInitialSpawn(@NotNull Player player, @NotNull Pos pos) {
        respawnHandler.prepareSpawn(player, pos);

        final BlockSumoInstance instance = game.getInstance();
        final Entity entity = new Entity(EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloudMeta) entity.getEntityMeta()).setRadius(0);
        entity.setNoGravity(true);
        entity.setInstance(instance, pos).thenRun(() -> entity.addPassenger(player));
    }

    public void registerGameListeners(@NotNull EventNode<Event> eventNode) {
        deathHandler.registerDeathListener(eventNode);
    }

    public void addInitialTags(@NotNull Player player) {
        player.setTag(PlayerTags.LAST_DAMAGE_TIME, 0L);
    }

    public @NotNull PlayerDeathHandler getDeathHandler() {
        return deathHandler;
    }

    public @NotNull PlayerRespawnHandler getRespawnHandler() {
        return respawnHandler;
    }

    public void broadcastMessage(@NotNull Component message) {
        game.getAudience().sendMessage(message);
    }
}
