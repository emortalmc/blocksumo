package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class EnderPearl extends PowerUp {
    private static final Component NAME = MiniMessage.miniMessage().deserialize("<gradient:blue:light_purple>Ender Pearl</gradient>");
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.ENDER_PEARL, NAME, ItemRarity.RARE);

    public EnderPearl(@NotNull BlockSumoGame game) {
        super(game, "ender_pearl", ITEM_INFO, SpawnLocation.CENTER);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        removeOneItemFromPlayer(player, hand);
        shootProjectile(player);
        playThrowSound(player);
    }

    private void shootProjectile(@NotNull Player thrower) {
        final EntityProjectile pearl = new EntityProjectile(thrower, EntityType.ENDER_PEARL);

        pearl.setTag(PowerUp.NAME, name);
        pearl.setBoundingBox(0.1, 0.1, 0.1);
        pearl.setVelocity(thrower.getPosition().direction().mul(35.0));
        pearl.setGravity(0.04, 0.04);

        final Instance instance = thrower.getInstance();
        pearl.setInstance(instance, thrower.getPosition().add(0, thrower.getEyeHeight(), 0));

        // TODO: Schedule cleanup task
    }

    private void playThrowSound(@NotNull Player thrower) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_ENDER_PEARL_THROW, Sound.Source.BLOCK, 1, 1);
        final Pos source = thrower.getPosition();
        game.getAudience().playSound(sound, source.x(), source.y(), source.z());
    }

    @Override
    public void onCollideWithBlock(@NotNull Player shooter, @NotNull Pos collisionPosition) {
        shooter.teleport(collisionPosition);
    }

    @Override
    public void onCollideWithEntity(@NotNull EntityProjectile entity, @NotNull Player shooter, @NotNull Player target,
                                    @NotNull Pos collisionPos) {
        onCollideWithBlock(shooter, collisionPos);
    }
}
