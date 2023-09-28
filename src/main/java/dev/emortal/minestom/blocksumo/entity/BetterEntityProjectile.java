package dev.emortal.minestom.blocksumo.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BetterEntityProjectile extends LivingEntity {

    protected final @Nullable Player shooter;

    private boolean hasDrag = true;
    private boolean hasGravityDrag = true;

    public BetterEntityProjectile(@Nullable Player shooter, @NotNull EntityType entityType) {
        super(entityType);

        this.shooter = shooter;
        super.hasPhysics = false;
    }

    public void collidePlayer(@NotNull Point pos, @NotNull Player player) {
    }

    public void collideBlock(@NotNull Point pos) {
    }

    @Override
    protected void updateVelocity(boolean wasOnGround, boolean flying, Pos positionBeforeMove, Vec newVelocity) {
        EntitySpawnType type = super.entityType.registry().spawnType();
        double airDrag = type == EntitySpawnType.LIVING || type == EntitySpawnType.PLAYER ? 0.91 : 0.98;

        double drag;
        if (wasOnGround) {
            Chunk chunk = ChunkUtils.retrieve(super.instance, super.currentChunk, super.position);
            synchronized (chunk) {
                drag = chunk.getBlock(positionBeforeMove.sub(0, 0.5000001, 0)).registry().friction() * airDrag;
            }
        } else {
            drag = airDrag;
        }

        double gravity = flying ? 0 : super.gravityAcceleration;
        double gravityDrag;

        if (!this.hasGravityDrag) {
            gravityDrag = 1.0;
        } else {
            gravityDrag = flying ? 0.6 : (1 - super.gravityDragPerTick);
        }
        if (!this.hasDrag) drag = 1.0;

        double finalDrag = drag;
        super.velocity = newVelocity
                // Apply gravity and drag
                .apply((x, y, z) -> new Vec(
                        x * finalDrag,
                        !super.hasNoGravity() ? (y - gravity) * gravityDrag : y,
                        z * finalDrag
                ))
                // Convert from block/tick to block/sec
                .mul(MinecraftServer.TICK_PER_SECOND)
                // Prevent infinitely decreasing velocity
                .apply(Vec.Operator.EPSILON);
    }

    @Override
    public void tick(long time) {
        Pos posBefore = super.getPosition();
        super.tick(time);
        Pos posNow = super.getPosition();

        Vec diff = Vec.fromPoint(posNow.sub(posBefore));
        PhysicsResult result = CollisionUtils.handlePhysics(
                super.instance, super.getChunk(),
                super.getBoundingBox(),
                posBefore, diff,
                null, true
        );

//        if (cooldown + 500 < System.currentTimeMillis()) {
//            float yaw = (float) Math.toDegrees(Math.atan2(diff.x(), diff.z()));
//            float pitch = (float) Math.toDegrees(Math.atan2(diff.y(), Math.sqrt(diff.x() * diff.x() + diff.z() * diff.z())));
//            super.refreshPosition(new Pos(posNow.x(), posNow.y(), posNow.z(), yaw, pitch));
//            cooldown = System.currentTimeMillis();
//        }

        PhysicsResult collided = CollisionUtils.checkEntityCollisions(super.instance, super.getBoundingBox(), posBefore, diff, 3,
                entity -> entity instanceof Player player && player != this.shooter && player.getGameMode() != GameMode.SPECTATOR, result);

        Shape shape = collided != null ? collided.collisionShapes()[0] : null;
        if (collided != null && shape != this.shooter) {
            if (shape instanceof Player player) {
                this.collidePlayer(collided.newPosition(), player);

//                var e = new ProjectileCollideWithEntityEvent(this, collided.newPosition(), player);
//                MinecraftServer.getGlobalEventHandler().call(e);
                return;
            }
        }

        if (result.hasCollision()) {
            Shape[] shapes = result.collisionShapes();
            Point[] points = result.collisionPoints();

            Block hitBlock = null;
            Point hitPoint = null;
            if (shapes[0] instanceof ShapeImpl block) {
                hitBlock = block.block();
                hitPoint = points[0];
            }
            if (shapes[1] instanceof ShapeImpl block) {
                hitBlock = block.block();
                hitPoint = points[1];
            }
            if (shapes[2] instanceof ShapeImpl block) {
                hitBlock = block.block();
                hitPoint = points[2];
            }

            if (hitBlock == null) return;

            this.collideBlock(hitPoint);

//            var e = new ProjectileCollideWithBlockEvent(this, Pos.fromPoint(hitPoint), hitBlock);
//            MinecraftServer.getGlobalEventHandler().call(e);
        }
    }

    public void setDrag(boolean drag) {
        this.hasDrag = drag;
    }

    public void setGravityDrag(boolean drag) {
        this.hasGravityDrag = drag;
    }
}
