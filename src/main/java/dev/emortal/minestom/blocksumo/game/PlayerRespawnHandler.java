package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.team.TeamColor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;
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

    private final @NotNull BlockSumoGame game;

    private final Map<UUID, Task> respawnTasks = new ConcurrentHashMap<>();

    public PlayerRespawnHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void scheduleRespawn(@NotNull Player player, @NotNull Runnable afterRespawnAction) {
        RespawnTask task = new RespawnTask(this.game, player, afterRespawnAction);
        this.respawnTasks.put(player.getUuid(), player.scheduler().submitTask(task));
    }

    public Block prepareSpawn(@NotNull Pos pos) {
        Instance instance = this.game.getSpawningInstance();

        Block replacedBlock = instance.getBlock(pos.add(0, -1, 0));
        instance.setBlock(pos.add(0, -1, 0), Block.BEDROCK);
        instance.setBlock(pos, Block.AIR);
        instance.setBlock(pos.add(0, 1, 0), Block.AIR);

        return replacedBlock;
    }

    public void stopAllScheduledRespawns() {
        for (Task task : this.respawnTasks.values()) {
            task.cancel();
        }
        this.respawnTasks.clear();
    }

    public void cleanUpPlayer(@NotNull Player player) {
        Task task = this.respawnTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }

    private final class RespawnTask implements Supplier<TaskSchedule> {

        private final @NotNull BlockSumoGame game;
        private final @NotNull Player player;
        private final @NotNull Runnable afterRespawnAction;

        private int i = 4;

        RespawnTask(@NotNull BlockSumoGame game, @NotNull Player player, @NotNull Runnable afterRespawnAction) {
            this.game = game;
            this.player = player;
            this.afterRespawnAction = afterRespawnAction;
        }

        @Override
        public @NotNull TaskSchedule get() {
            if (this.i == 4) {
                // Wait 2 seconds so that the death title is cleared before we count down until respawn.
                this.i--;
                return TaskSchedule.seconds(2);
            }

            if (this.i == 0) {
                this.respawn();
                this.afterRespawnAction.run();
                return TaskSchedule.stop();
            }

            this.playPrepareRespawnSound();
            this.showCountdownTitle(this.i);
            this.i--;
            return TaskSchedule.seconds(1);
        }

        private void playPrepareRespawnSound() {
            this.player.playSound(
                    Sound.sound(SoundEvent.BLOCK_METAL_BREAK, Sound.Source.BLOCK, 1, 2),
                    Sound.Emitter.self()
            );
        }

        private void showCountdownTitle(int countdown) {
            Title title = Title.title(
                    Component.text(countdown, TextColor.lerp(countdown / 3F, NamedTextColor.GREEN, NamedTextColor.RED), TextDecoration.BOLD),
                    Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))
            );
            this.player.showTitle(title);
        }

        private void respawn() {
            Pos respawnPos = this.game.getSpawnHandler().getBestRespawn();

            this.player.teleport(respawnPos).thenRun(() -> {
                this.reset();
                this.updateLivesInHealth();
            });

            this.playRespawnSound();
            this.player.setTag(PlayerTags.CAN_BE_HIT, true);
            this.player.setTag(PlayerTags.LAST_DAMAGE_TIME, 0L);
            this.player.setCanPickupItem(true);

            this.game.getSpawnProtectionManager().startProtection(this.player, 4000);

            this.prepareRespawn(respawnPos, 5);
            this.giveWoolAndShears();
            this.giveColoredChestplate();
        }

        private void reset() {
            this.player.getEntityMeta().setNotifyAboutChanges(false);

            this.player.getInventory().clear();
            this.player.setAutoViewable(true);
            this.player.setInvisible(false);
            this.player.setGlowing(false);
            this.player.setSneaking(false);
            this.player.setAllowFlying(false);
            this.player.setFlying(false);
            this.player.setAdditionalHearts(0);
            this.player.setGameMode(GameMode.SURVIVAL);
            this.player.setFood(20);
            this.player.setLevel(0);

            if (this.player.getVehicle() != null) {
                this.player.getVehicle().removePassenger(this.player);
            }

            this.player.setArrowCount(0);
            this.player.setFireForDuration(0);
            this.player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1F);
            this.player.setCanPickupItem(true);

            if (this.player.getOpenInventory() != null) {
                this.player.closeInventory();
            }

            this.player.setNoGravity(false);
            this.player.heal();

            this.player.getEntityMeta().setNotifyAboutChanges(true);

            this.player.clearEffects();
            this.player.stopSpectating();
            this.player.stopSound(SoundStop.all());

            this.player.updateViewableRule($ -> true);
            this.player.updateViewerRule($ -> true);
        }

        private void updateLivesInHealth() {
            int lives = this.player.getTag(PlayerTags.LIVES);
            float health = lives * 2;

            this.player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
            this.player.setHealth(this.player.getMaxHealth());
        }

        private void playRespawnSound() {
            this.player.playSound(
                    Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 1, 2),
                    Sound.Emitter.self()
            );
        }

        private void prepareRespawn(@NotNull Pos pos, int restoreDelay) {
            Block replacedBlock = PlayerRespawnHandler.this.prepareSpawn(pos);

            Instance instance = this.game.getSpawningInstance();
            MinecraftServer.getSchedulerManager()
                    .buildTask(() -> instance.setBlock(pos.blockX(), pos.blockY() - 1, pos.blockZ(), replacedBlock.isAir() ? Block.WHITE_WOOL : replacedBlock))
                    .delay(restoreDelay, ChronoUnit.SECONDS)
                    .schedule();

            // TODO: Spawn protection
        }

        private void giveWoolAndShears() {
            TeamColor color = this.player.getTag(PlayerTags.TEAM_COLOR);
            this.player.getInventory().setItemStack(0, ItemStack.of(Material.SHEARS, 1));
            this.player.getInventory().setItemStack(1, color.getWoolItem());
        }

        private void giveColoredChestplate() {
            TeamColor color = this.player.getTag(PlayerTags.TEAM_COLOR);
            ItemStack chestplate = ItemStack.builder(Material.LEATHER_CHESTPLATE)
                    .meta(LeatherArmorMeta.class, meta -> meta.color(new Color(color.getColor())))
                    .build();
            this.player.getInventory().setChestplate(chestplate);
        }
    }
}
