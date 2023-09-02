package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.entity.FishingBobberManager;
import dev.emortal.minestom.blocksumo.event.EventManager;
import dev.emortal.minestom.blocksumo.explosion.ExplosionManager;
import dev.emortal.minestom.blocksumo.map.LoadedMap;
import dev.emortal.minestom.blocksumo.map.MapData;
import dev.emortal.minestom.blocksumo.powerup.PowerUpManager;
import dev.emortal.minestom.blocksumo.team.TeamColor;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
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
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockSumoGame extends Game {
    public static final int MIN_PLAYERS = 2;
    public static final @NotNull Component TITLE =
            MiniMessage.miniMessage().deserialize("<gradient:blue:aqua><bold>Block Sumo</bold></gradient>");

    private final PlayerManager playerManager;
    private final EventManager eventManager;
    private final PowerUpManager powerUpManager;
    private final PlayerSpawnHandler spawnHandler;
    private final ExplosionManager explosionManager;
    private final FishingBobberManager bobberManager;
    private final SpawnProtectionManager spawnProtectionManager;
    private final @NotNull Instance instance;
    private final @NotNull MapData mapData;

    private Task countdownTask;

    public BlockSumoGame(@NotNull GameCreationInfo creationInfo, @NotNull LoadedMap map) {
        super(creationInfo);
        this.instance = map.instance();
        this.mapData = map.mapData();

        this.playerManager = new PlayerManager(this, 49);

        this.eventManager = new EventManager(this);
        this.eventManager.registerDefaultEvents();

        this.powerUpManager = new PowerUpManager(this);
        this.powerUpManager.registerDefaultPowerUps();
        this.spawnHandler = new PlayerSpawnHandler(this, List.copyOf(mapData.spawns()));
        this.explosionManager = new ExplosionManager(this);
        this.bobberManager = new FishingBobberManager(this);
        this.spawnProtectionManager = new SpawnProtectionManager();

        this.playerManager.registerPreGameListeners(getEventNode());
        this.playerManager.setupWaitingScoreboard();

        if (MinestomGameServer.TEST_MODE) {
            MinecraftServer.getSchedulerManager()
                    .buildTask(this::sendSpawnPacketsToPlayers)
                    .delay(3, ChronoUnit.SECONDS)
                    .repeat(1, ChronoUnit.SECONDS)
                    .schedule();
        }
    }

    @Override
    public void onJoin(Player player) {
        player.setRespawnPoint(this.spawnHandler.getBestSpawn());
        player.setAutoViewable(true);
        this.playerManager.addInitialTags(player);
    }

    @Override
    public void onLeave(@NotNull Player player) {
        this.playerManager.getDisconnectHandler().onDisconnect(player);
    }

    private void sendSpawnPacketsToPlayers() {
        final Set<ServerPacket> packets = createSpawnPackets();
        for (final ServerPacket packet : packets) {
            PacketUtils.sendGroupedPacket(this.players, packet);
        }
    }

    private Set<ServerPacket> createSpawnPackets() {
        final Consumer<BinaryWriter> extraDataWriter = writer -> {
            writer.writeFloat(1);
            writer.writeFloat(0);
            writer.writeFloat(0);
            writer.writeFloat(1.5F);
        };

        final Set<ServerPacket> packets = new HashSet<>();
        for (final Pos spawn : this.mapData.spawns()) {
            final ServerPacket packet = ParticleCreator.createParticlePacket(Particle.DUST, true,
                    spawn.x(), spawn.y(), spawn.z(),
                    0, 0, 0,
                    0, 1, extraDataWriter);
            packets.add(packet);
        }

        return packets;
    }

    @Override
    public void start() {
        this.playSound(Sound.sound(SoundEvent.BLOCK_PORTAL_TRIGGER, Sound.Source.MASTER, 0.45f, 1.27f));

        this.countdownTask = this.instance.scheduler().submitTask(new Supplier<>() {
            int i = 3;

            @Override
            public TaskSchedule get() {
                if (this.i == 0) {
                    BlockSumoGame.this.showGameStartTitle();
                    BlockSumoGame.this.startGame();
                    return TaskSchedule.stop();
                }

                showCountdown(this.i);
                this.i--;
                return TaskSchedule.seconds(1);
            }
        });
    }

    private void startGame() {
        this.playerManager.registerGameListeners(getEventNode());
        this.powerUpManager.registerListeners(getEventNode());
        this.removeLockingEntities();
        for (final Player player : this.getPlayers()) {
            this.giveWoolAndShears(player);
            this.giveColoredChestplate(player);
            this.setSpawnBlockToWool(player);
        }
        this.eventManager.startRandomEventTask();
        this.powerUpManager.startRandomPowerUpTasks();
    }

    private void showCountdown(final int countdown) {
        this.playSound(Sound.sound(Key.key("battle.countdown.begin"), Sound.Source.MASTER, 1F, 1F), Sound.Emitter.self());
        this.showTitle(Title.title(
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
        this.showTitle(title);
    }

    private void removeLockingEntities() {
        for (final Entity entity : instance.getEntities()) {
            if (entity.getEntityType() == EntityType.AREA_EFFECT_CLOUD) entity.remove();
        }
    }

    private void giveWoolAndShears(@NotNull Player player) {
        final TeamColor color = player.getTag(PlayerTags.TEAM_COLOR);
        player.getInventory().setItemStack(0, ItemStack.of(Material.SHEARS, 1));
        player.getInventory().setItemStack(1, color.getWoolItem());
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

    public void cancelCountdown() {
        if (this.countdownTask != null) this.countdownTask.cancel();
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

        for (final Player player : this.players) {
            if (winners.contains(player)) {
                player.showTitle(victoryTitle);
            } else {
                player.showTitle(defeatTitle);
            }
        }

        this.instance.scheduler().buildTask(this::sendBackToLobby).delay(TaskSchedule.seconds(6)).schedule();
    }

    private void sendBackToLobby() {
        finish();
    }

    @Override
    public void cleanUp() {
        for (final Player player : this.players) {
            player.kick(Component.text("The game ended but we weren't able to connect you to a lobby. Please reconnect.", NamedTextColor.RED));
        }
        this.instance.scheduler().scheduleNextTick(() -> {
            MinecraftServer.getInstanceManager().unregisterInstance(this.instance);
        });
        this.playerManager.cleanUp();
    }

    @Override
    public @NotNull Instance getSpawningInstance() {
        return this.instance;
    }

    public @NotNull EventManager getEventManager() {
        return this.eventManager;
    }

    public @NotNull PowerUpManager getPowerUpManager() {
        return this.powerUpManager;
    }

    public @NotNull PlayerSpawnHandler getSpawnHandler() {
        return this.spawnHandler;
    }

    public @NotNull ExplosionManager getExplosionManager() {
        return this.explosionManager;
    }

    public @NotNull FishingBobberManager getBobberManager() {
        return this.bobberManager;
    }

    public @NotNull SpawnProtectionManager getSpawnProtectionManager() {
        return this.spawnProtectionManager;
    }
}
