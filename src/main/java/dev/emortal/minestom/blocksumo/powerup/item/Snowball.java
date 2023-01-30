package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class Snowball extends PowerUp {
    private static final Component NAME = Component.text("Snowball", NamedTextColor.AQUA);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.SNOWBALL, NAME, ItemRarity.COMMON, 8);

    public Snowball(@NotNull BlockSumoGame game) {
        super(game, "snowball", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        removeOneItemFromPlayer(player, hand);
        shootProjectile(player);
        playThrowSound(player);
    }

    private void shootProjectile(@NotNull Player thrower) {
        final EntityProjectile snowball = new EntityProjectile(thrower, EntityType.SNOWBALL);

        snowball.setTag(PowerUp.NAME, name);
        snowball.setBoundingBox(0.1, 0.1, 0.1);
        snowball.setVelocity(thrower.getPosition().direction().mul(30.0));

        final Instance instance = thrower.getInstance();
        snowball.scheduleRemove(10, TimeUnit.SECOND);
        snowball.setInstance(instance, thrower.getPosition().add(0, thrower.getEyeHeight(), 0));
    }

    private void playThrowSound(@NotNull Player thrower) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_SNOWBALL_THROW, Sound.Source.BLOCK, 1, 1);
        final Pos source = thrower.getPosition();
        game.getAudience().playSound(sound, source.x(), source.y(), source.z());
    }

    @Override
    public void onCollideWithEntity(@NotNull Player shooter, @NotNull Player target, @NotNull Pos collisionPos) {
        // TODO: Take knockback from collision pos
    }
}
