package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.powerup.item.*;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerUpManager {

    private final PowerUpRegistry registry;
    private final BlockSumoGame game;
    private final RandomPowerUpHandler randomPowerUpHandler;

    public PowerUpManager(@NotNull BlockSumoGame game) {
        this.registry = new PowerUpRegistry();
        this.game = game;
        this.randomPowerUpHandler = new RandomPowerUpHandler(game, this);
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerUseItemEvent.class, this::onItemUse);

        eventNode.addListener(PlayerUseItemOnBlockEvent.class, event -> {
            final Player player = event.getPlayer();
            final Player.Hand hand = event.getHand();

            final PowerUp heldPowerUp = getHeldPowerUp(player, hand);
            if (heldPowerUp != null) heldPowerUp.onUseOnBlock(player, hand);
        });

        eventNode.addListener(ProjectileCollideWithBlockEvent.class, this::onCollideWithBlock);
        eventNode.addListener(ProjectileCollideWithEntityEvent.class, this::onCollideWithEntity);
        randomPowerUpHandler.registerListeners(eventNode);
    }

    private void onItemUse(@NotNull PlayerUseItemEvent event) {
        event.setCancelled(true);

        final Player player = event.getPlayer();
        final Player.Hand hand = event.getHand();

        final PowerUp heldPowerUp = getHeldPowerUp(player, hand);

        if (heldPowerUp instanceof GrapplingHook) {
            event.setCancelled(false);
            game.getBobberManager().cast(player, hand);
            return;
        }

        if (heldPowerUp != null) heldPowerUp.onUse(player, hand);
    }

    private void onCollideWithBlock(@NotNull ProjectileCollideWithBlockEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile entity)) return;
        if (!(entity.getShooter() instanceof Player shooter)) return;

        final String powerUpName = getPowerUpName(entity);
        final PowerUp powerUp = findNamedPowerUp(powerUpName);
        if (powerUp == null) return;

        powerUp.onCollideWithBlock(shooter, event.getCollisionPosition());
        if (powerUp.shouldRemoveEntityOnCollision()) entity.remove();
    }

    private void onCollideWithEntity(@NotNull ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile entity)) return;
        if (!(entity.getShooter() instanceof Player shooter)) return;
        if (!(event.getTarget() instanceof Player target)) return;

        if (game.getSpawnProtectionManager().isProtected(target)) {
            game.getSpawnProtectionManager().notifyProtected(shooter, target);
            return;
        }

        if (!target.getTag(PlayerTags.CAN_BE_HIT)) return;
        target.setTag(PlayerTags.CAN_BE_HIT, false);
        target.scheduler().buildTask(() -> target.setTag(PlayerTags.CAN_BE_HIT, true)).delay(TaskSchedule.tick(10)).schedule();

        target.damage(DamageType.fromPlayer(shooter), 0);

        final String powerUpName = getPowerUpName(entity);
        final PowerUp powerUp = findNamedPowerUp(powerUpName);
        if (powerUp == null) return;

        powerUp.onCollideWithEntity(entity, shooter, target, event.getCollisionPosition());
        if (powerUp.shouldRemoveEntityOnCollision()) entity.remove();
    }

    public void startRandomPowerUpTasks() {
        randomPowerUpHandler.startRandomPowerUpTasks();
    }

    public void registerDefaultPowerUps() {
        registry.registerPowerUp(new Puncher(game));
        registry.registerPowerUp(new Slimeball(game));
        registry.registerPowerUp(new Snowball(game));
        registry.registerPowerUp(new EnderPearl(game));
        registry.registerPowerUp(new TNT(game));
        registry.registerPowerUp(new AntiGravityTNT(game));
        registry.registerPowerUp(new KnockbackStick(game));
        registry.registerPowerUp(new Fireball(game));
        registry.registerPowerUp(new Switcheroo(game));
        registry.registerPowerUp(new GrapplingHook(game));
    }

    public @Nullable PowerUp findNamedPowerUp(@NotNull String id) {
        return registry.findByName(id);
    }

    public @NotNull PowerUp findRandomPowerUp(@NotNull SpawnLocation spawnLocation) {
        final List<PowerUp> possiblePowerUps = registry.findAllBySpawnLocation(spawnLocation);
        final int totalWeight = calculateTotalWeight(possiblePowerUps);

        int index = 0;
        int randomIndex = ThreadLocalRandom.current().nextInt(totalWeight + 1);
        while (index < possiblePowerUps.size() - 1) {
            randomIndex -= possiblePowerUps.get(index).getRarity().getWeight();
            if (randomIndex <= 0) break;
            ++index;
        }

        return possiblePowerUps.get(index);
    }

    private int calculateTotalWeight(@NotNull List<PowerUp> powerUps) {
        int totalWeight = 0;
        for (final PowerUp powerUp : powerUps) {
            totalWeight += powerUp.getRarity().getWeight();
        }
        return totalWeight;
    }

    public @NotNull PowerUp findRandomPowerUp() {
        return registry.findRandom();
    }

    public @Nullable PowerUp getHeldPowerUp(@NotNull Player player, @NotNull Player.Hand hand) {
        final ItemStack heldItem = player.getItemInHand(hand);
        final String powerUpId = getPowerUpName(heldItem);
        return registry.findByName(powerUpId);
    }

    private @NotNull String getPowerUpName(@NotNull TagReadable powerUp) {
        return powerUp.getTag(PowerUp.NAME);
    }

    public void givePowerUp(@NotNull Player player, @NotNull PowerUp powerUp) {
        final ItemStack item = powerUp.createItemStack();
        player.getInventory().addItemStack(item);
    }

    public @NotNull Collection<String> getPowerUpIds() {
        return registry.getPowerUpNames();
    }
}
