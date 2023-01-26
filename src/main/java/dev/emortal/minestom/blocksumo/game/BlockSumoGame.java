package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.gamesdk.config.GameCreationInfo;
import dev.emortal.minestom.gamesdk.game.Game;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockSumoGame extends Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockSumoGame.class);

    private List<Pos> availableSpawns;

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final @NotNull EventNode<Event> eventNode;
    private final @NotNull CompletableFuture<BlockSumoInstance> instanceFuture;

    public BlockSumoGame(@NotNull GameCreationInfo creationInfo, @NotNull EventNode<Event> gameEventNode,
                         @NotNull CompletableFuture<BlockSumoInstance> instanceFuture) {
        super(creationInfo);

        this.instanceFuture = instanceFuture;
        this.instanceFuture.thenAccept(instance -> this.availableSpawns = new ArrayList<>(instance.getMapData().spawns()));

        this.eventNode = EventNode.event(UUID.randomUUID().toString(), EventFilter.ALL, event -> {
            if (event instanceof PlayerEvent playerEvent) {
                if (!this.getGameCreationInfo().playerIds().contains(playerEvent.getPlayer().getUuid())) return false;
            }
            if (event instanceof InstanceEvent instanceEvent) {
                // we don't have to worry about instance events if the instance isn't loaded yet
                if (instanceFuture.isDone()) {
                    BlockSumoInstance instance = instanceFuture.join();
                    return instanceEvent.getInstance() == instance;
                }
            }
            return true;
        });
        gameEventNode.addChild(this.eventNode);

        eventNode.addListener(PlayerLoginEvent.class, event -> {
            // TODO remove
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            event.getPlayer().setFlying(true);

            BlockSumoInstance instance = this.instanceFuture.join();

            Player player = event.getPlayer();
            if (!creationInfo.playerIds().contains(player.getUuid())) {
                player.kick("Unexpected join (" + Environment.getHostname() + ")");
                LOGGER.info("Unexpected join for player {}", player.getUuid());
                return;
            }

            player.setRespawnPoint(this.getSpawnPos());
            event.setSpawningInstance(instance);
            this.players.add(player);

            player.setAutoViewable(true);
        });

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            this.prepareSpawn(player, player.getRespawnPoint());
        });

        eventNode.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            Pos oldPos = player.getPosition();
            if (oldPos.x() != event.getNewPosition().x() || oldPos.z() != event.getNewPosition().z()) {
//                event.setCancelled(true); TODO uncomment
            }
        });

        this.instanceFuture.thenAccept(instance -> {
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                Set<SendablePacket> packets = this.createSpawnPackets();
                for (Player player : this.players) {
                    packets.forEach(player::sendPacket);
                }
            }).delay(3, ChronoUnit.SECONDS).repeat(1, ChronoUnit.SECONDS).schedule();
        });
    }

    private Set<SendablePacket> createSpawnPackets() {
        Set<SendablePacket> packets = new HashSet<>();
        for (Pos spawn : this.instanceFuture.join().getMapData().spawns()) {
            packets.add(ParticleCreator.createParticlePacket(Particle.DUST, true,
                    spawn.x(), spawn.y(), spawn.z(),
                    0, 0, 0, 0f, 1,
                    binaryWriter -> {
                        binaryWriter.writeFloat(1);
                        binaryWriter.writeFloat(0);
                        binaryWriter.writeFloat(0);
                        binaryWriter.writeFloat(1.5f);
                    }));
        }

        return packets;
    }

    /**
     * Gets an available spawn position for when a player initially
     * spawns at the start of the game.
     * <p>
     * Now this may seem like it functions very weirdly but here's the goal.
     * Select the furthest away point from the mean position of all players to achieve a balanced spread.
     *
     * @return the spawn position
     */
    private synchronized @NotNull Pos getSpawnPos() {
        BlockSumoInstance instance = this.instanceFuture.join();

        Pos bestPos = null;
        double bestWorstDistance = 0; // The best of the worst distances
        for (Pos pos : this.availableSpawns) {
            // Measure a worst-case scenario for each point
            double closestDistance = Double.MAX_VALUE;
            for (final Player player : getPlayers()) {
                final Pos playerPos = player.getPosition();
                double distance = playerPos.distanceSquared(pos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                }
            }
            if (closestDistance > bestWorstDistance) {
                bestPos = pos;
                bestWorstDistance = closestDistance;
            }
        }

        final Pos spawnPos;
        if (bestPos == null) {
            LOGGER.warn("No available spawns left for instance {}", instance.getUniqueId());
            spawnPos = this.availableSpawns.get(0);
        } else {
            spawnPos = bestPos;
        }
        return spawnPos;
    }

    private void prepareSpawn(@NotNull Player player, @NotNull Pos pos) {
        BlockSumoInstance instance = this.instanceFuture.join();

        Pos bedrockPos = pos.add(0, -1, 0);

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

        final BlockSumoInstance instance = this.instanceFuture.join();
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> instance.setBlock(pos.sub(1), Block.WHITE_WOOL))
                .delay(restoreDelay, ChronoUnit.SECONDS)
                .schedule();
    }

    @Override
    public void load() {
        this.instanceFuture.join();
    }

    @Override
    public void start() {

    }

    @Override
    public void cancel() {

    }

    public @NotNull CompletableFuture<BlockSumoInstance> getInstanceFuture() {
        return instanceFuture;
    }
}
