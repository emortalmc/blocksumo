package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import dev.emortal.minestom.blocksumo.utils.KnockbackUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.SnowballMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class Slimeball extends PowerUp {
    private static final Component NAME = Component.text("Slimeball", NamedTextColor.GREEN);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.SLIME_BALL, NAME, ItemRarity.COMMON, 8);

    private static final Tag<Double> POSITION_X_TAG = Tag.Double("thrower_position_x");
    private static final Tag<Double> POSITION_Y_TAG = Tag.Double("thrower_position_y");
    private static final Tag<Double> POSITION_Z_TAG = Tag.Double("thrower_position_z");

    private static final ItemStack SLIME_ITEM = ItemStack.of(Material.SLIME_BALL);

    public Slimeball(@NotNull BlockSumoGame game) {
        super(game, "slimeball", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        removeOneItemFromPlayer(player, hand);
        shootProjectile(player);
        playThrowSound(player);
    }

    private void shootProjectile(@NotNull Player thrower) {
        final EntityProjectile snowball = new EntityProjectile(thrower, EntityType.SNOWBALL);
        ((SnowballMeta) snowball.getEntityMeta()).setItem(SLIME_ITEM);

        snowball.setTag(PowerUp.NAME, name);
        snowball.setTag(POSITION_X_TAG, thrower.getPosition().x());
        snowball.setTag(POSITION_Y_TAG, thrower.getPosition().y());
        snowball.setTag(POSITION_Z_TAG, thrower.getPosition().z());

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
    public void onCollideWithEntity(@NotNull EntityProjectile entity, @NotNull Player shooter, @NotNull Player target,
                                    @NotNull Pos collisionPos) {
        Pos throwerPos = new Pos(
                entity.getTag(POSITION_X_TAG),
                entity.getTag(POSITION_Y_TAG),
                entity.getTag(POSITION_Z_TAG)
        );

        KnockbackUtil.takeKnockback(target, throwerPos, -1);
    }
}
