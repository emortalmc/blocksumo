package dev.emortal.minestom.blocksumo.game;

import dev.emortal.api.kurushimi.KurushimiUtils;
import dev.emortal.minestom.blocksumo.event.EventManager;
import dev.emortal.minestom.blocksumo.explosion.ExplosionManager;
import dev.emortal.minestom.blocksumo.map.LoadedMap;
import dev.emortal.minestom.blocksumo.map.MapData;
import dev.emortal.minestom.blocksumo.powerup.PowerUpManager;
import dev.emortal.minestom.blocksumo.team.TeamColor;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameCreationInfo;
import dev.emortal.minestom.gamesdk.game.Game;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class BlockSumoGame extends Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockSumoGame.class);
    public static final @NotNull Component TITLE =
            MiniMessage.miniMessage().deserialize("<gradient:blue:aqua><bold>Block Sumo</bold></gradient>");

    private final PlayerManager playerManager;
    private final EventManager eventManager;
    private final PowerUpManager powerUpManager;
    private final PlayerSpawnHandler spawnHandler;
    private final ExplosionManager explosionManager;

    private final @NotNull EventNode<Event> eventNode;
    private final @NotNull Instance instance;
    private final @NotNull MapData mapData;

    public BlockSumoGame(@NotNull GameCreationInfo creationInfo, @NotNull EventNode<Event> gameEventNode, @NotNull LoadedMap map) {
        super(creationInfo, gameEventNode);
        this.playerManager = new PlayerManager(this, 49);

        this.eventManager = new EventManager(this);
        eventManager.registerDefaultEvents();

        this.powerUpManager = new PowerUpManager(this);
        powerUpManager.registerDefaultPowerUps();

        this.instance = map.instance();
        this.mapData = map.mapData();
        this.spawnHandler = new PlayerSpawnHandler(this, List.copyOf(mapData.spawns()));
        this.explosionManager = new ExplosionManager(this);

        this.eventNode = EventNode.event(UUID.randomUUID().toString(), EventFilter.ALL, event -> {
            if (event instanceof PlayerEvent playerEvent) {
                if (!isValidPlayerForGame(playerEvent.getPlayer())) return false;
            }
            if (event instanceof InstanceEvent instanceEvent) {
                return instanceEvent.getInstance() == instance;
            }
            return true;
        });
        gameEventNode.addChild(this.eventNode);
        playerManager.registerPreGameListeners(eventNode);
        playerManager.setupWaitingScoreboard();

        MinecraftServer.getSchedulerManager()
                .buildTask(this::sendSpawnPacketsToPlayers)
                .delay(3, ChronoUnit.SECONDS)
                .repeat(1, ChronoUnit.SECONDS)
                .schedule();
    }

    private boolean isValidPlayerForGame(@NotNull Player player) {
        return getGameCreationInfo().playerIds().contains(player.getUuid());
    }

    @Override
    public void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!getGameCreationInfo().playerIds().contains(player.getUuid())) {
            player.kick("Unexpected join (" + Environment.getHostname() + ")");
            LOGGER.info("Unexpected join for player {}", player.getUuid());
            return;
        }

        player.setRespawnPoint(spawnHandler.getBestSpawn());
        event.setSpawningInstance(instance);
        this.players.add(player);

        player.setAutoViewable(true);
        playerManager.addInitialTags(player);
    }

    private void sendSpawnPacketsToPlayers() {
        final Set<SendablePacket> packets = this.createSpawnPackets();
        for (final Player player : players) {
            packets.forEach(player::sendPacket);
        }
    }

    private Set<SendablePacket> createSpawnPackets() {
        Set<SendablePacket> packets = new HashSet<>();
        for (final Pos spawn : mapData.spawns()) {
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

    @Override
    public void start() {
        audience.playSound(Sound.sound(SoundEvent.BLOCK_PORTAL_TRIGGER, Sound.Source.MASTER, 0.45f, 1.27f));

        playerManager.getScoreboard().removeLine("infoLine");
        instance.scheduler().submitTask(new Supplier<>() {
            int i = 3;

            @Override
            public TaskSchedule get() {
                if (i == 0) {
                    showGameStartTitle();
                    startGame();
                    return TaskSchedule.stop();
                }

                showCountdown(i);
                i--;
                return TaskSchedule.seconds(1);
            }
        });
    }

    private void startGame() {
        playerManager.registerGameListeners(eventNode);
        powerUpManager.registerListeners(eventNode);
        removeLockingEntities();
        for (final Player player : getPlayers()) {
            giveWoolAndShears(player);
            giveColoredChestplate(player);
            setSpawnBlockToWool(player);
        }
    }

    private void showCountdown(final int countdown) {
        audience.playSound(Sound.sound(Key.key("battle.countdown.begin"), Sound.Source.MASTER, 1F, 1F), Sound.Emitter.self());
        audience.showTitle(Title.title(
                Component.text(countdown, NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500))
        ));
    }

    private void showGameStartTitle() {
        final Title title = Title.title(
                Component.text("GO!", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ZERO)
        );
        audience.showTitle(title);
    }

    private void removeLockingEntities() {
        instance.getEntities().forEach(entity -> {
            if (entity.getEntityType() == EntityType.AREA_EFFECT_CLOUD) entity.remove();
        });
    }

    private void giveWoolAndShears(@NotNull Player player) {
        TeamColor team = player.getTag(PlayerTags.TEAM_COLOR);

        player.getInventory().setItemStack(0, ItemStack.of(Material.SHEARS, 1));
        player.getInventory().setItemStack(1, team.getWoolItem());
    }

    private void giveColoredChestplate(@NotNull Player player) {
        final TeamColor color = player.getTag(PlayerTags.TEAM_COLOR);
        final ItemStack chestplate = ItemStack.builder(Material.LEATHER_CHESTPLATE)
                .meta(LeatherArmorMeta.class, meta -> meta.color(new Color(color.getColor())))
                .build();
        player.getInventory().setChestplate(chestplate);
    }

    private void setSpawnBlockToWool(@NotNull Player player) {
        final Pos pos = player.getPosition();
        instance.setBlock(pos.blockX(), pos.blockY() - 1, pos.blockZ(), Block.WHITE_WOOL);
    }

    public void victory(final @NotNull Set<Player> winners) {
        final Title victoryTitle = Title.title(
                MiniMessage.miniMessage().deserialize("<gradient:#ffc570:gold><bold>VICTORY!</bold></gradient>"),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(4))
        );
        final Title defeatTitle = Title.title(
                MiniMessage.miniMessage().deserialize("<gradient:#ff474e:#ff0d0d><bold>DEFEAT!</bold></gradient>"),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(4))
        );

        for (final Player player : players) {
            if (winners.contains(player)) {
                player.showTitle(victoryTitle);
            } else {
                player.showTitle(defeatTitle);
            }
        }

        instance.scheduler().buildTask(this::sendBackToLobby).delay(TaskSchedule.seconds(6)).schedule();
    }

    @Override
    public void cancel() {
        LOGGER.warn("Game cancelled early. Sending players back to lobby.");
        sendBackToLobby();
    }

    private void sendBackToLobby() {
        KurushimiUtils.sendToLobby(players, this::removeGame, this::removeGame);
    }

    private void removeGame() {
        GameSdkModule.getGameManager().removeGame(this);
        cleanUp();
    }

    private void cleanUp() {
        for (final Player player : players) {
            player.kick(Component.text("The game ended but we weren't able to connect you to a lobby. Please reconnect.", NamedTextColor.RED));
        }
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
        playerManager.cleanUp();
    }

    public @NotNull Instance getInstance() {
        return instance;
    }

    public @NotNull EventManager getEventManager() {
        return eventManager;
    }

    public @NotNull PowerUpManager getPowerUpManager() {
        return powerUpManager;
    }

    public @NotNull PlayerSpawnHandler getSpawnHandler() {
        return spawnHandler;
    }

    public @NotNull ExplosionManager getExplosionManager() {
        return explosionManager;
    }
}
