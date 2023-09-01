package dev.emortal.minestom.blocksumo.entity;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.item.GrapplingHook;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FishingBobberManager {
    private static final float MAX_VELOCITY = 0.4F;

    private final BlockSumoGame game;

    private final Map<UUID, FishingBobber> bobbers = new HashMap<>();
    private final Map<UUID, Player> hooked = new HashMap<>();

    public FishingBobberManager(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void setHooked(@NotNull Player caster, @NotNull Player hooked) {
        this.hooked.put(caster.getUuid(), hooked);
    }

    public void cast(@NotNull Player caster, @NotNull Player.Hand hand) {
        final UUID casterId = caster.getUuid();
        if (bobbers.containsKey(casterId)) {
            retract(caster, hand);
            return;
        }

        final Pos castPos = calculateCastPos(caster);
        final FishingBobber bobber = createBobber(caster, castPos);
        castBobberInCastDirection(bobber, caster);
    }

    private @NotNull Pos calculateCastPos(@NotNull Player caster) {
        final float yaw = caster.getPosition().yaw();

        final double directionX = Math.sin(Math.toRadians(-yaw) - Math.PI);
        final double directionZ = Math.cos(Math.toRadians(-yaw) - Math.PI);

        final double x = caster.getPosition().x() - directionX * 0.3;
        final double y = caster.getPosition().y() + caster.getEyeHeight();
        final double z = caster.getPosition().z() - directionZ * 0.3;
        return new Pos(x, y, z);
    }

    private @NotNull FishingBobber createBobber(@NotNull Player caster, @NotNull Pos castPos) {
        final FishingBobber bobber = new FishingBobber(this, caster);
        bobber.setTag(PowerUp.NAME, "grappling_hook");
        bobber.setInstance(game.getSpawningInstance(), castPos);
        bobbers.put(caster.getUuid(), bobber);
        return bobber;
    }

    private void castBobberInCastDirection(@NotNull FishingBobber bobber, @NotNull Player caster) {
        final Pos casterPos = caster.getPosition();
        final float pitch = casterPos.pitch();
        final float yaw = casterPos.yaw();

        final Vec velocity = new Vec(
                -Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * MAX_VELOCITY,
                -Math.sin(pitch / 180 * Math.PI) * MAX_VELOCITY,
                Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * MAX_VELOCITY
        );
        bobber.setVelocity(velocity.normalize().mul(45));
    }

    public void retract(@NotNull Player caster, @NotNull Player.Hand hand) {
        final FishingBobber bobber = bobbers.get(caster.getUuid());
        if (bobber == null) return;

        final PowerUp heldPowerUp = game.getPowerUpManager().getHeldPowerUp(caster, hand);
        if (heldPowerUp instanceof GrapplingHook hook) {
            hook.onRetract(caster, hand, bobber.getPosition(), hooked.get(caster.getUuid()));
        }

        bobber.doRemove();
    }

    void removeBobber(@NotNull Player shooter) {
        final UUID shooterId = shooter.getUuid();
        bobbers.remove(shooterId);
        hooked.remove(shooterId);
    }
}
