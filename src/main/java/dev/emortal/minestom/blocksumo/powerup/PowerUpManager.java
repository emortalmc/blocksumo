package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.powerup.item.*;
import dev.emortal.minestom.blocksumo.powerup.item.hook.GrapplingHook;
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
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerUpManager {

    private final @NotNull PowerUpRegistry registry;
    private final @NotNull BlockSumoGame game;
    private final @NotNull RandomPowerUpHandler randomPowerUpHandler;

    public PowerUpManager(@NotNull BlockSumoGame game) {
        this.registry = new PowerUpRegistry();
        this.game = game;
        this.randomPowerUpHandler = new RandomPowerUpHandler(game, this);
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerUseItemEvent.class, this::onItemUse);
        eventNode.addListener(PlayerUseItemOnBlockEvent.class, this::onItemUseOnBlock);

        eventNode.addListener(ProjectileCollideWithBlockEvent.class, this::onCollideWithBlock);
        eventNode.addListener(ProjectileCollideWithEntityEvent.class, this::onCollideWithEntity);

        this.randomPowerUpHandler.registerListeners(eventNode);
    }

    private void onItemUse(@NotNull PlayerUseItemEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Player.Hand hand = event.getHand();

        PowerUp heldPowerUp = this.getHeldPowerUp(player, hand);
        if (heldPowerUp != null) heldPowerUp.onUse(player, hand);
    }

    private void onItemUseOnBlock(@NotNull PlayerUseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        Player.Hand hand = event.getHand();

        PowerUp heldPowerUp = this.getHeldPowerUp(player, hand);
        if (heldPowerUp != null) heldPowerUp.onUseOnBlock(player, hand);
    }

    private void onCollideWithBlock(@NotNull ProjectileCollideWithBlockEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile entity)) return;
        if (!(entity.getShooter() instanceof Player shooter)) return;

        String powerUpName = this.getPowerUpName(entity);
        PowerUp powerUp = this.findNamedPowerUp(powerUpName);
        if (powerUp == null) return;

        powerUp.onCollideWithBlock(shooter, event.getCollisionPosition());
        if (powerUp.shouldRemoveEntityOnCollision()) entity.remove();
    }

    private void onCollideWithEntity(@NotNull ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile entity)) return;
        if (!(entity.getShooter() instanceof Player shooter)) return;
        if (!(event.getTarget() instanceof Player target)) return;

        if (this.game.getSpawnProtectionManager().isProtected(target)) {
            this.game.getSpawnProtectionManager().notifyProtected(shooter, target);
            return;
        }

        if (!target.getTag(PlayerTags.CAN_BE_HIT)) return;
        target.setTag(PlayerTags.CAN_BE_HIT, false);

        target.scheduler()
                .buildTask(() -> target.setTag(PlayerTags.CAN_BE_HIT, true))
                .delay(TaskSchedule.tick(10))
                .schedule();

        target.damage(DamageType.fromPlayer(shooter), 0);
        HitAnimationPacket hitAnimationPacket = new HitAnimationPacket(target.getEntityId(), entity.getPosition().yaw());
        this.game.sendGroupedPacket(hitAnimationPacket);

        String powerUpName = this.getPowerUpName(entity);
        PowerUp powerUp = this.findNamedPowerUp(powerUpName);
        if (powerUp == null) return;

        powerUp.onCollideWithEntity(entity, shooter, target, event.getCollisionPosition());
        if (powerUp.shouldRemoveEntityOnCollision()) entity.remove();
    }

    public void startRandomPowerUpTasks() {
        this.randomPowerUpHandler.startRandomPowerUpTasks();
    }

    public void registerDefaultPowerUps() {
        this.registry.registerPowerUp(new Puncher(this.game));
        this.registry.registerPowerUp(new Slimeball(this.game));
        this.registry.registerPowerUp(new Snowball(this.game));
        this.registry.registerPowerUp(new EnderPearl(this.game));
        this.registry.registerPowerUp(new Tnt(this.game));
        this.registry.registerPowerUp(new AntiGravityTnt(this.game));
        this.registry.registerPowerUp(new KnockbackStick(this.game));
        this.registry.registerPowerUp(new Fireball(this.game));
        this.registry.registerPowerUp(new Switcheroo(this.game));
        this.registry.registerPowerUp(new GrapplingHook(this.game));
    }

    public @Nullable PowerUp findNamedPowerUp(@NotNull String id) {
        return this.registry.findByName(id);
    }

    public @NotNull PowerUp findRandomPowerUp(@NotNull SpawnLocation spawnLocation) {
        List<PowerUp> possiblePowerUps = this.registry.findAllBySpawnLocation(spawnLocation);
        int totalWeight = this.calculateTotalWeight(possiblePowerUps);

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
        for (PowerUp powerUp : powerUps) {
            totalWeight += powerUp.getRarity().getWeight();
        }
        return totalWeight;
    }

    public @NotNull PowerUp findRandomPowerUp() {
        return this.registry.findRandom();
    }

    public @Nullable PowerUp getHeldPowerUp(@NotNull Player player, @NotNull Player.Hand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        String powerUpId = this.getPowerUpName(heldItem);
        return this.registry.findByName(powerUpId);
    }

    private @NotNull String getPowerUpName(@NotNull TagReadable powerUp) {
        return powerUp.getTag(PowerUp.NAME);
    }

    public void givePowerUp(@NotNull Player player, @NotNull PowerUp powerUp) {
        ItemStack item = powerUp.createItemStack();
        player.getInventory().addItemStack(item);
    }

    public @NotNull Collection<String> getPowerUpIds() {
        return this.registry.getPowerUpNames();
    }
}
