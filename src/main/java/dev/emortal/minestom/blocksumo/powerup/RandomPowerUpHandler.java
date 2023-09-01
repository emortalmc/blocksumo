package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapData;
import dev.emortal.minestom.blocksumo.utils.FireworkUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.item.firework.FireworkEffectType;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class RandomPowerUpHandler {
    private static final Pos FIREWORK_CENTER = MapData.CENTER.add(0, 1, 0);

    private final BlockSumoGame game;
    private final PowerUpManager powerUpManager;

    public RandomPowerUpHandler(@NotNull BlockSumoGame game, @NotNull PowerUpManager powerUpManager) {
        this.game = game;
        this.powerUpManager = powerUpManager;
    }

    public void startRandomPowerUpTasks() {
        final Instance instance = game.getSpawningInstance();

        instance.scheduler().buildTask(this::spawnRandomCenterPowerUp)
                .delay(TaskSchedule.seconds(10))
                .repeat(TaskSchedule.seconds(30))
                .schedule();

        instance.scheduler().buildTask(this::giveRandomPowerUpToAll)
                .delay(TaskSchedule.seconds(5))
                .repeat(TaskSchedule.seconds(45))
                .schedule();
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PickupItemEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            final boolean added = player.getInventory().addItemStack(event.getItemStack());
            if (added) {
                playPickupSound(player);
            } else {
                event.setCancelled(true);
            }
        });
    }

    private void playPickupSound(@NotNull Player player) {
        player.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 1, 1));
    }

    private void spawnRandomCenterPowerUp() {
        final ItemStack powerUp = powerUpManager.findRandomPowerUp(SpawnLocation.CENTER).createItemStack();

        final ItemEntity entity = new ItemEntity(powerUp);
        final ItemEntityMeta meta = entity.getEntityMeta();
        meta.setItem(powerUp);
        meta.setCustomName(powerUp.getDisplayName());
        meta.setCustomNameVisible(true);

        entity.setNoGravity(true);
        entity.setMergeable(false);
        entity.setPickupDelay(5, TimeUnit.CLIENT_TICK);
        entity.setBoundingBox(0.5, 0.25, 0.5);
        entity.setInstance(game.getSpawningInstance(), MapData.CENTER);

        notifySpawned(powerUp);
        displaySpawnedFirework();
    }

    private void giveRandomPowerUpToAll() {
        final PowerUp powerUp = powerUpManager.findRandomPowerUp(SpawnLocation.ANYWHERE);
        final ItemStack powerUpItem = powerUp.createItemStack();

        for (final Player player : game.getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) continue;
            player.getInventory().addItemStack(powerUpItem);
        }

        notifyGiven(powerUp);
        playGivenSound();
    }

    private void notifySpawned(@NotNull ItemStack powerUp) {
        final Component message = Component.text()
                .append(Objects.requireNonNull(powerUp.getDisplayName()))
                .append(Component.text(" has spawned at the center!", NamedTextColor.GRAY))
                .build();
        game.sendMessage(message);
    }

    private void notifyGiven(@NotNull PowerUp powerUp) {
        final Component message = Component.text()
                .append(powerUp.getItemName())
                .append(Component.text(" has been given to everyone!", NamedTextColor.GRAY))
                .build();
        game.sendMessage(message);
    }

    private void displaySpawnedFirework() {
        final FireworkEffect effect = new FireworkEffect(false, false, FireworkEffectType.SMALL_BALL,
                List.of(new Color(255, 100, 0)), List.of(new Color(255, 0, 255)));
        FireworkUtil.showFirework(game.getPlayers(), game.getSpawningInstance(), FIREWORK_CENTER, List.of(effect));
    }

    private void playGivenSound() {
        game.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 1, 1));
    }
}
