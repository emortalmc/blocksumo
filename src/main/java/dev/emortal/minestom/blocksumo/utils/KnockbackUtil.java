package dev.emortal.minestom.blocksumo.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class KnockbackUtil {
    private static final int TPS = MinecraftServer.TICK_PER_SECOND;
    private static final double HORIZONTAL_KNOCKBACK = 0.4 * TPS;
    private static final double VERTICAL_KNOCKBACK = HORIZONTAL_KNOCKBACK;
    private static final double EXTRA_HORIZONTAL_KNOCKBACK = 0.5 * TPS;
    private static final double EXTRA_VERTICAL_KNOCKBACK = 0.1 * TPS;
    private static final double LIMIT_VERTICAL_KNOCKBACK = 0.5 * TPS;

    public static void takeKnockback(@NotNull Entity target, @NotNull Pos position, double strength) {
        final double horizontalKnockback = 0.25 * TPS;

        final double d0 = position.x() - target.getPosition().x();
        final double d1 = position.z() - target.getPosition().z();
        final double magnitude = Math.sqrt(d0 * d0 + d1 * d1) * strength;

        final Vec velocity = target.getVelocity();
        final double newVelocityX = magnitude != 0.0 ? ((velocity.x() / 2) - (d0 / magnitude * horizontalKnockback)) : velocity.x();
        double newVelocityY = Math.min((velocity.y() / 2) + 8, 8);
        final double newVelocityZ = magnitude != 0.0 ? ((velocity.z() / 2) - (d1 / magnitude * horizontalKnockback)) : velocity.z();

        if (newVelocityY > 8) newVelocityY = 8;
        target.setVelocity(new Vec(newVelocityX, newVelocityY, newVelocityZ));
    }

    public static void takeKnockback(@NotNull Player source, @NotNull Player target) {
        double d0 = source.getPosition().x() - target.getPosition().x();
        double d1 = source.getPosition().z() - target.getPosition().z();
        while (d0 * d0 + d1 * d1 < 1.0E-4) {
            d0 = (Math.random() - Math.random()) * 0.01;
            d1 = (Math.random() - Math.random()) * 0.01;
        }

        final double magnitude = Math.sqrt(d0 * d0 + d1 * d1);

        double knockbackLevel = getKnockbackLevel(source);
        if (source.isSprinting()) knockbackLevel += 1.0;

        final Vec velocity = target.getVelocity();

        final double newVelocityX = (velocity.x() / 2) - (d0 / magnitude * HORIZONTAL_KNOCKBACK);
        final double newVelocityZ = (velocity.z() / 2) - (d1 / magnitude * HORIZONTAL_KNOCKBACK);

        double newVelocityY = (velocity.y() / 2) + VERTICAL_KNOCKBACK;
        if (newVelocityY > LIMIT_VERTICAL_KNOCKBACK) newVelocityY = LIMIT_VERTICAL_KNOCKBACK;

        Vec newVelocity = new Vec(newVelocityX, newVelocityY, newVelocityZ);
        if (knockbackLevel > 0) {
            newVelocity = newVelocity.add(
                    -Math.sin(Math.toRadians(source.getPosition().yaw())) * (knockbackLevel * EXTRA_HORIZONTAL_KNOCKBACK),
                    EXTRA_VERTICAL_KNOCKBACK,
                    Math.cos(Math.toRadians(source.getPosition().yaw())) * (knockbackLevel * EXTRA_HORIZONTAL_KNOCKBACK)
            );
        }

        target.setVelocity(newVelocity);
    }

    private static double getKnockbackLevel(@NotNull Player source) {
        final ItemStack mainHand = source.getItemInMainHand();
        final Map<Enchantment, Short> enchantments = mainHand.meta().getEnchantmentMap();
        final Short level = enchantments.get(Enchantment.KNOCKBACK);
        return level != null ? (double) level : 0.0;
    }

    private KnockbackUtil() {
    }
}
