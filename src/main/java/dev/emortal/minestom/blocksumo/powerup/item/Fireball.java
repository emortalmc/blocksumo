package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.entity.NoDragEntity;
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
    public static final Tag<String> SHOOTER = Tag.String("shooter");

    public Fireball(@NotNull BlockSumoGame game) {
        super(game, "fireball", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        removeOneItemFromPlayer(player, hand);

        final Entity fireball = shootFireball(player);
        final Vec originalVelocity = player.getPosition().direction().mul(20);
        fireball.setVelocity(originalVelocity);

        playShootingSound(player.getPosition());

        fireball.scheduler().submitTask(() -> {
            if (fireball.getAliveTicks() > 5L * MinecraftServer.TICK_PER_SECOND) {
                fireball.remove();
                return TaskSchedule.stop();
            }

            if (!fireball.getVelocity().sameBlock(originalVelocity)) {
                collide(fireball);
                return TaskSchedule.stop();
            }

            final Player firstCollider = findFirstCollider(fireball, player);
            if (firstCollider != null) {
                collide(fireball);
                return TaskSchedule.stop();
            }

            showCollisionParticle(fireball);
            return TaskSchedule.nextTick();
        });
    }

    private @NotNull Entity shootFireball(@NotNull Player shooter) {
        final Entity fireball = new NoDragEntity(EntityType.FIREBALL);
        fireball.setNoGravity(true);
        fireball.setTag(PowerUp.NAME, name);
        fireball.setTag(SHOOTER, shooter.getUsername());
        fireball.setBoundingBox(0.6, 0.6, 0.6);
        fireball.setInstance(game.getSpawningInstance(), shooter.getPosition().add(0, shooter.getEyeHeight(), 0));
        return fireball;
    }

    private void playShootingSound(@NotNull Point source) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_GHAST_SHOOT, Sound.Source.BLOCK, 1, 1);
        game.playSound(sound, source.x(), source.y(), source.z());
    }

    private void collide(@NotNull Entity entity) {
        final Player shooter = findShooter(entity);
        game.getExplosionManager().explode(entity.getPosition(), EXPLOSION, shooter, entity);
        entity.remove();
    }

    private @NotNull Player findShooter(@NotNull Entity entity) {
        final String shooterName = entity.getTag(SHOOTER);

        for (final Player player : game.getPlayers()) {
            if (player.getUsername().equals(shooterName)) return player;
        }
        throw new IllegalStateException("Shooter " + shooterName + " not found!");
    }

    private @Nullable Player findFirstCollider(@NotNull Entity fireball, @NotNull Player shooter) {
        for (final Player player : game.getPlayers()) {
            if (player == shooter) continue;
            final Pos position = player.getPosition();
            final BoundingBox box = player.getBoundingBox();
            if (box.intersectEntity(position, fireball)) return player;
        }
        return null;
    }

    private void showCollisionParticle(@NotNull Entity fireball) {
        final double posX = fireball.getPosition().x();
        final double posY = fireball.getPosition().y();
        final double posZ = fireball.getPosition().z();
        final ParticlePacket packet = ParticleCreator.createParticlePacket(Particle.LARGE_SMOKE, posX, posY, posZ, 0, 0, 0, 1);
        game.sendGroupedPacket(packet);
    }

    @Override
    public void onUseOnBlock(@NotNull Player player, @NotNull Player.Hand hand) {
        onUse(player, hand);
    }

    @Override
    public boolean shouldHandleBlockPlace() {
        return true;
    }

    @Override
    public void onBlockPlace(@NotNull Player player, @NotNull Player.Hand hand, @NotNull Point clickedPos) {
        onUse(player, hand);
    }
}
