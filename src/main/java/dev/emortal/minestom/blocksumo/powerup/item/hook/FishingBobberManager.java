package dev.emortal.minestom.blocksumo.powerup.item.hook;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class FishingBobberManager {
    private static final float MAX_VELOCITY = 0.4F;

    private final @NotNull BlockSumoGame game;

    private final Map<UUID, FishingBobber> bobbers = new HashMap<>();
    private final Map<UUID, Player> hooked = new HashMap<>();

    FishingBobberManager(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    void setHooked(@NotNull Player caster, @NotNull Player hooked) {
        this.hooked.put(caster.getUuid(), hooked);
    }

    void cast(@NotNull Player caster, @NotNull Player.Hand hand) {
        UUID casterId = caster.getUuid();
        if (this.bobbers.containsKey(casterId)) {
            this.retract(caster, hand);
            return;
        }

        Pos castPos = this.calculateCastPos(caster);
        FishingBobber bobber = this.createBobber(caster, castPos);
        this.castBobberInCastDirection(bobber, caster);
    }

    private @NotNull Pos calculateCastPos(@NotNull Player caster) {
        float yaw = caster.getPosition().yaw();

        double directionX = Math.sin(Math.toRadians(-yaw) - Math.PI);
        double directionZ = Math.cos(Math.toRadians(-yaw) - Math.PI);

        double x = caster.getPosition().x() - directionX * 0.3;
        double y = caster.getPosition().y() + caster.getEyeHeight();
        double z = caster.getPosition().z() - directionZ * 0.3;
        return new Pos(x, y, z);
    }

    private @NotNull FishingBobber createBobber(@NotNull Player caster, @NotNull Pos castPos) {
        FishingBobber bobber = new FishingBobber(this, caster);

        bobber.setTag(PowerUp.NAME, "grappling_hook");
        bobber.setInstance(this.game.getSpawningInstance(), castPos);

        this.bobbers.put(caster.getUuid(), bobber);
        return bobber;
    }

    private void castBobberInCastDirection(@NotNull FishingBobber bobber, @NotNull Player caster) {
        Pos casterPos = caster.getPosition();
        float pitch = casterPos.pitch();
        float yaw = casterPos.yaw();

        Vec velocity = new Vec(
                -Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * MAX_VELOCITY,
                -Math.sin(pitch / 180 * Math.PI) * MAX_VELOCITY,
                Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * MAX_VELOCITY
        );
        bobber.setVelocity(velocity.normalize().mul(45));
    }

    void retract(@NotNull Player caster, @NotNull Player.Hand hand) {
        FishingBobber bobber = this.bobbers.get(caster.getUuid());
        if (bobber == null) return;

        PowerUp heldPowerUp = this.game.getPowerUpManager().getHeldPowerUp(caster, hand);
        if (heldPowerUp instanceof GrapplingHook hook) {
            hook.onRetract(caster, hand, bobber.getPosition(), this.hooked.get(caster.getUuid()));
        }

        bobber.remove();
    }

    void removeBobber(@NotNull Player shooter) {
        UUID shooterId = shooter.getUuid();
        this.bobbers.remove(shooterId);
        this.hooked.remove(shooterId);
    }
}
