package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PlayerRespawnHandler {

    private final BlockSumoGame game;
    private final PlayerTracker playerTracker;
    private final Map<UUID, Task> respawnTasks = new ConcurrentHashMap<>();

    public PlayerRespawnHandler(@NotNull BlockSumoGame game, @NotNull PlayerTracker playerTracker) {
        this.game = game;
        this.playerTracker = playerTracker;
    }

    public void scheduleRespawn(@NotNull Player player) {
        final Task task = player.scheduler().submitTask(new Supplier<>() {
            int i = 4;

            @Override
            public TaskSchedule get() {
                if (i == 4) {
                    // Wait 2 seconds so that the death title is cleared before we count down until respawn.
                    i--;
                    return TaskSchedule.seconds(2);
                }

                if (i == 0) {
                    respawn(player);
                    return TaskSchedule.stop();
                }

                playPrepareRespawnSound(player);
                showCountdownTitle(player, i);
                i--;
                return TaskSchedule.seconds(1);
            }
        });
        respawnTasks.put(player.getUuid(), task);
    }

    private void playPrepareRespawnSound(@NotNull Player player) {
        player.playSound(
                Sound.sound(SoundEvent.BLOCK_METAL_BREAK, Sound.Source.BLOCK, 1, 2),
                Sound.Emitter.self()
        );
    }

    private void showCountdownTitle(@NotNull Player player, int countdown) {
        final Title title = Title.title(
                Component.text(countdown, TextColor.lerp(countdown / 3F, NamedTextColor.GREEN, NamedTextColor.RED), TextDecoration.BOLD),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))
        );
        player.showTitle(title);
    }

    private void respawn(@NotNull Player player) {
        final Pos respawnPos = game.getBestSpawnPos();

        player.teleport(respawnPos).thenRun(() -> {
            reset(player);
            // TODO: Set to lives left when we track lives
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
            player.setHealth(player.getMaxHealth());
        });

        playRespawnSound(player);
        player.setCanPickupItem(true);

        prepareRespawn(player, respawnPos, 5);
    }

    private void reset(@NotNull Player player) {
        player.getEntityMeta().setNotifyAboutChanges(false);

        player.getInventory().clear();
        player.setAutoViewable(true);
        player.setInvisible(false);
        player.setGlowing(false);
        player.setSneaking(false);
        player.setAllowFlying(false);
        player.setFlying(false);
        player.setAdditionalHearts(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFood(20);
        player.setLevel(0);

        if (player.getVehicle() != null) {
            player.getVehicle().removePassenger(player);
        }

        player.setArrowCount(0);
        player.setFireForDuration(0);
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1F);
        player.setCanPickupItem(true);

        if (player.getOpenInventory() != null) {
            player.closeInventory();
        }

        player.setNoGravity(false);
        player.heal();

        player.getEntityMeta().setNotifyAboutChanges(true);

        player.clearEffects();
        player.stopSpectating();
        player.stopSound(SoundStop.all());

        player.updateViewableRule($ -> true);
        player.updateViewerRule($ -> true);
    }

    private void playRespawnSound(@NotNull Player player) {
        player.playSound(
                Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 1, 2),
                Sound.Emitter.self()
        );
    }

    public void prepareSpawn(@NotNull Player player, @NotNull Pos pos) {
        final BlockSumoInstance instance = game.getInstance();

        instance.setBlock(pos.blockX(), pos.blockY() - 1, pos.blockZ(), Block.BEDROCK);
        instance.setBlock(pos.blockX(), pos.blockY() + 1, pos.blockZ(), Block.AIR);
        instance.setBlock(pos.blockX(), pos.blockY() + 2, pos.blockZ(), Block.AIR);
    }

    private void prepareRespawn(@NotNull Player player, @NotNull Pos pos, int restoreDelay) {
        prepareSpawn(player, pos);

        final BlockSumoInstance instance = game.getInstance();
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> instance.setBlock(pos.blockX(), pos.blockY() - 1, pos.blockZ(), Block.WHITE_WOOL))
                .delay(restoreDelay, ChronoUnit.SECONDS)
                .schedule();

        // TODO: Spawn protection
    }
}
