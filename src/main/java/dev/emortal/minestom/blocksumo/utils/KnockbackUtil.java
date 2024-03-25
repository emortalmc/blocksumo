package dev.emortal.minestom.blocksumo.utils;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class KnockbackUtil {
    private static final int TPS = ServerFlag.SERVER_TICKS_PER_SECOND;
    private static final double HORIZONTAL_KNOCKBACK = 0.4 * TPS;
    private static final double VERTICAL_KNOCKBACK = HORIZONTAL_KNOCKBACK;
    private static final double EXTRA_HORIZONTAL_KNOCKBACK = 0.5 * TPS;
    private static final double EXTRA_VERTICAL_KNOCKBACK = 0.1 * TPS;
    private static final double LIMIT_VERTICAL_KNOCKBACK = 0.5 * TPS;

    public static void takeKnockback(@NotNull Entity target, @NotNull Vec direction, double strength) {
        Vec newVelocity = target.getVelocity()
                .mul(0.5)
                .add(direction.mul(strength * HORIZONTAL_KNOCKBACK))
                .add(0, VERTICAL_KNOCKBACK, 0)
                .withY(y -> Math.min(y, LIMIT_VERTICAL_KNOCKBACK)); // cap Y at LIMIT_VERTICAL_KNOCKBACK;

        target.setVelocity(newVelocity);
    }
    public static void takeKnockback(@NotNull Entity target, @NotNull Point position, double strength) {
        takeKnockback(target, Vec.fromPoint(target.getPosition().withY(0).sub(position.withY(0))).normalize(), strength);
    }
    public static void takeKnockback(@NotNull Player source, @NotNull Player target) {
        Point sourceNoY = source.getPosition().withY(0);
        Point targetNoY = target.getPosition().withY(0);

        Vec direction = Vec.fromPoint(targetNoY.sub(sourceNoY)).normalize();

        double knockbackLevel = getKnockbackLevel(source);
        if (source.isSprinting()) {
            knockbackLevel += 1.0;
        }

        Vec newVelocity = target.getVelocity()
                .mul(0.5)
                .add(direction.mul(HORIZONTAL_KNOCKBACK))
                .add(0, VERTICAL_KNOCKBACK, 0)
                .withY(y -> Math.min(y, LIMIT_VERTICAL_KNOCKBACK)) // cap Y at LIMIT_VERTICAL_KNOCKBACK
                .add(direction.mul(knockbackLevel * EXTRA_HORIZONTAL_KNOCKBACK))
                .add(0, knockbackLevel > 0 ? EXTRA_VERTICAL_KNOCKBACK : 0, 0);

        target.setVelocity(newVelocity);
    }

    private static double getKnockbackLevel(@NotNull Player source) {
        ItemStack mainHand = source.getItemInMainHand();
        Map<Enchantment, Short> enchantments = mainHand.meta().getEnchantmentMap();
        Short level = enchantments.get(Enchantment.KNOCKBACK);
        return level != null ? (double) level : 0.0;
    }

    private KnockbackUtil() {
    }
}
