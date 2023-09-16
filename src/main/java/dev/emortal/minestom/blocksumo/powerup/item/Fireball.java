package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.entity.BetterEntityProjectile;
import dev.emortal.minestom.blocksumo.explosion.ExplosionData;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Fireball extends PowerUp {
    private static final Component NAME = Component.text("Fireball", NamedTextColor.GOLD);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.FIRE_CHARGE, NAME, ItemRarity.COMMON);

    private static final ExplosionData EXPLOSION = new ExplosionData(3, 35, 5.5, true);
    public static final @NotNull Tag<String> SHOOTER = Tag.String("shooter");

    public Fireball(@NotNull BlockSumoGame game) {
        super(game, "fireball", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        this.removeOneItemFromPlayer(player, hand);

        Entity fireball = this.shootFireball(player);
        Vec originalVelocity = player.getPosition().direction().mul(30);
        fireball.setVelocity(originalVelocity);

        this.playShootingSound(player.getPosition());

        fireball.scheduler().submitTask(() -> {
            if (fireball.getAliveTicks() > 5L * MinecraftServer.TICK_PER_SECOND) {
                fireball.remove();
                return TaskSchedule.stop();
            }

            if (!fireball.getVelocity().sameBlock(originalVelocity)) {
                this.collide(fireball);
                return TaskSchedule.stop();
            }

            Player firstCollider = this.findFirstCollider(fireball, player);
            if (firstCollider != null) {
                this.collide(fireball);
                return TaskSchedule.stop();
            }

            this.showCollisionParticle(fireball);
            return TaskSchedule.nextTick();
        });
    }

    private @NotNull Entity shootFireball(@NotNull Player shooter) {
        Entity fireball = new FireballEntity(shooter);

        fireball.setInstance(this.game.getSpawningInstance(), shooter.getPosition().add(0, shooter.getEyeHeight(), 0));

        return fireball;
    }

    private void playShootingSound(@NotNull Point source) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_GHAST_SHOOT, Sound.Source.BLOCK, 1, 1);
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }

    private void collide(@NotNull Entity entity) {
        Player shooter = this.findShooter(entity);
        this.game.getExplosionManager().explode(entity.getPosition(), EXPLOSION, shooter, entity);
        entity.remove();
    }

    private @NotNull Player findShooter(@NotNull Entity entity) {
        String shooterName = entity.getTag(SHOOTER);

        for (Player player : this.game.getPlayers()) {
            if (player.getUsername().equals(shooterName)) return player;
        }
        throw new IllegalStateException("Shooter " + shooterName + " not found!");
    }

    private @Nullable Player findFirstCollider(@NotNull Entity fireball, @NotNull Player shooter) {
        for (Player player : this.game.getPlayers()) {
            if (player == shooter) continue;

            Pos position = player.getPosition();
            BoundingBox box = player.getBoundingBox();
            if (box.intersectEntity(position, fireball)) return player;
        }
        return null;
    }

    private void showCollisionParticle(@NotNull Entity fireball) {
        double posX = fireball.getPosition().x();
        double posY = fireball.getPosition().y();
        double posZ = fireball.getPosition().z();
        ParticlePacket packet = ParticleCreator.createParticlePacket(Particle.LARGE_SMOKE, posX, posY, posZ, 0, 0, 0, 1);
        this.game.sendGroupedPacket(packet);
    }

    @Override
    public void onUseOnBlock(@NotNull Player player, @NotNull Player.Hand hand) {
        this.onUse(player, hand);
    }

    @Override
    public boolean shouldHandleBlockPlace() {
        return true;
    }

    @Override
    public void onBlockPlace(@NotNull Player player, @NotNull Player.Hand hand, @NotNull Point clickedPos) {
        this.onUse(player, hand);
    }

    private class FireballEntity extends BetterEntityProjectile {

        public FireballEntity(Player player) {
            super(player, EntityType.FIREBALL);

            setDrag(false);
            setGravityDrag(false);
            setNoGravity(true);
            setBoundingBox(0.6, 0.6, 0.6);
            setTag(PowerUp.NAME, name);
            setTag(SHOOTER, shooter.getUsername());
        }

        @Override
        public void collideBlock(Point pos) {
            collide(this);
        }

        @Override
        public void collidePlayer(Point pos, Player player) {
            collide(this);
        }
    }

}
