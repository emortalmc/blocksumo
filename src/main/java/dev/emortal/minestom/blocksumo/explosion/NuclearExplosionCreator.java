package dev.emortal.minestom.blocksumo.explosion;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public final class NuclearExplosionCreator {

    private final @NotNull BlockSumoGame game;
    private final @NotNull Point origin;
    private final @NotNull Entity source;
    private final int radius;
    private final @NotNull NuclearExplosionData data;

    public NuclearExplosionCreator(@NotNull BlockSumoGame game, @NotNull Point origin, @NotNull Entity source, int radius) {
        this.game = game;
        this.origin = origin;
        this.source = source;
        this.radius = radius;
        this.data = NuclearExplosionData.fromRadius(radius * radius);
    }

    public void explode() {
        for (Player player : this.game.getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) continue;
            this.applyEffects(player);
        }
    }

    private void applyEffects(@NotNull Player player) {
        double distance = player.getPosition().distanceSquared(this.origin);
        if (!this.isWithinRange(distance)) return;

        if (distance <= this.data.dangerRadius()) {
            this.applyDangerEffects(player);
        } else if (distance <= this.data.severeRadius()) {
            this.applySevereEffects(player);
        } else if (distance <= this.data.moderateRadius()) {
            this.applyModerateEffects(player);
        } else if (distance <= this.data.lightRadius()) {
            this.applyLightEffects(player);
        }
    }

    private boolean isWithinRange(double distanceFromOrigin) {
        return distanceFromOrigin <= this.radius * this.radius;
    }

    private void applyDangerEffects(@NotNull Player player) {
        player.damage(DamageType.fromEntity(this.source), 0);
    }

    private void applySevereEffects(@NotNull Player player) {
        player.addEffect(new Potion(PotionEffect.BLINDNESS, (byte) 0, 6 * 20));
        // TODO: Apply knockback
    }

    private void applyModerateEffects(@NotNull Player player) {
        player.addEffect(new Potion(PotionEffect.NAUSEA, (byte) 0, 4 * 20));
        // TODO: Apply knockback
    }

    private void applyLightEffects(@NotNull Player player) {
        player.addEffect(new Potion(PotionEffect.NAUSEA, (byte) 0, 4 * 20));
        // TODO: Apply knockback
    }

    private record NuclearExplosionData(double dangerRadius, double severeRadius, double moderateRadius, double lightRadius) {

        static @NotNull NuclearExplosionData fromRadius(double radius) {
            return new NuclearExplosionData(
                    radius * 0.285,
                    radius * 0.357,
                    radius * 0.571,
                    radius
            );
        }
    }
}
