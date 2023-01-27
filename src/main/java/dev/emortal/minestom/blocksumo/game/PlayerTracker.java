package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

public final class PlayerTracker {

    private final BlockSumoGame game;
    private final PlayerDeathHandler deathHandler;

    public PlayerTracker(@NotNull BlockSumoGame game, int minAllowedHeight) {
        this.game = game;
        this.deathHandler = new PlayerDeathHandler(minAllowedHeight);
    }

    public void registerPreGameListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            this.prepareSpawn(player, player.getRespawnPoint());
        });
    }

    public void registerGameListeners(@NotNull EventNode<Event> eventNode) {
        deathHandler.registerDeathListener(eventNode);
    }

    public void addInitialTags(@NotNull Player player) {
        player.setTag(PlayerTags.LAST_DAMAGE_TIME, 0L);
    }

    private void prepareSpawn(@NotNull Player player, @NotNull Pos pos) {
        final BlockSumoInstance instance = game.getInstance();

        final Pos bedrockPos = pos.add(0, -1, 0);

        instance.setBlock(bedrockPos, Block.BEDROCK);
        instance.setBlock(pos.add(0, 1, 0), Block.AIR);
        instance.setBlock(pos.add(0, 2, 0), Block.AIR);

        final Entity entity = new Entity(EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloudMeta) entity.getEntityMeta()).setRadius(0);
        entity.setNoGravity(true);
        entity.setInstance(instance, pos).thenRun(() -> entity.addPassenger(player));
    }

    private void prepareRespawn(@NotNull Player player, @NotNull Pos pos, int restoreDelay) {
        this.prepareSpawn(player, pos);

        final BlockSumoInstance instance = game.getInstance();
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> instance.setBlock(pos.sub(1), Block.WHITE_WOOL))
                .delay(restoreDelay, ChronoUnit.SECONDS)
                .schedule();
    }
}
