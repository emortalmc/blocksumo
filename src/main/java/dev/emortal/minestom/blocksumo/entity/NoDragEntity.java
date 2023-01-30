package dev.emortal.minestom.blocksumo.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public final class NoDragEntity extends Entity {

    public NoDragEntity(EntityType type) {
        super(type);
    }

    @Override
    protected void updateVelocity(boolean wasOnGround, boolean flying, Pos positionBeforeMove, Vec newVelocity) {
        if (newVelocity == null) return;

        // Multiply by TPS to get blocks per second from blocks per tick
        // Apply epsilon to prevent infinitely decreasing velocity
        final Vec velocity = newVelocity.mul(MinecraftServer.TICK_PER_SECOND).apply(Vec.Operator.EPSILON);
        setVelocity(velocity);
    }
}
