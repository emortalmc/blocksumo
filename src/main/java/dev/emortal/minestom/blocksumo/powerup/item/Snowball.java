package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.entity.BetterEntityProjectile;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import dev.emortal.minestom.blocksumo.utils.KnockbackUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
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
        this.removeOneItemFromPlayer(player, hand);
        this.shootProjectile(player);
        this.playThrowSound(player);
    }

    private void shootProjectile(@NotNull Player shooter) {
        SnowballEntity snowball = new SnowballEntity(shooter);

        Instance instance = shooter.getInstance();
        snowball.scheduleRemove(10, TimeUnit.SECOND);
        snowball.setInstance(instance, shooter.getPosition().add(0, shooter.getEyeHeight(), 0));
    }

    private void playThrowSound(@NotNull Player thrower) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_SNOWBALL_THROW, Sound.Source.BLOCK, 1, 1);
        Pos source = thrower.getPosition();
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }

    private final class SnowballEntity extends BetterEntityProjectile {
        private final Player shooter;
        public SnowballEntity(@NotNull Player shooter) {
            super(shooter, EntityType.SNOWBALL);

            this.shooter = shooter;

            setTag(PowerUp.NAME, Snowball.super.name);
            setBoundingBox(0.25, 0.25, 0.25);
            setVelocity(shooter.getPosition().direction().mul(30.0));
        }

        @Override
        public void collidePlayer(@NotNull Point pos, @NotNull Player player) {
            KnockbackUtil.takeKnockback(player, this.position.direction(), 1);
            Snowball.super.game.getPlayerManager().getDamageHandler().damage(player, shooter, false);
            remove();
        }

        @Override
        public void collideBlock(@NotNull Point pos) {
            remove();
        }
    }
}
