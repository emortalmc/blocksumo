package dev.emortal.minestom.blocksumo.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BetterEntityProjectile extends LivingEntity {

    private long cooldown = 0;
    protected final Player shooter;
    private boolean ticking = true;
    private boolean hasDrag = true;
    private boolean hasGravityDrag = true;


    public BetterEntityProjectile(@Nullable Player shooter, @NotNull EntityType entityType) {
        super(entityType);

        this.shooter = shooter;
        this.hasPhysics = false;
    }

    public void collidePlayer(Point pos, Player player) {
    }

    public void collideBlock(Point pos) {
    }

    @Override
    protected void updateVelocity(boolean wasOnGround, boolean flying, Pos positionBeforeMove, Vec newVelocity) {
        EntitySpawnType type = entityType.registry().spawnType();
        final double airDrag = type == EntitySpawnType.LIVING || type == EntitySpawnType.PLAYER ? 0.91 : 0.98;
        double drag;
        if (wasOnGround) {
            final Chunk chunk = ChunkUtils.retrieve(instance, currentChunk, position);
            synchronized (chunk) {
                drag = chunk.getBlock(positionBeforeMove.sub(0, 0.5000001, 0)).registry().friction() * airDrag;
            }
        } else {
            drag = airDrag;
        }

        double gravity = flying ? 0 : gravityAcceleration;
        double gravityDrag;

        if (!hasGravityDrag) {
            gravityDrag = 1.0;
        } else {
            gravityDrag = flying ? 0.6 : (1 - gravityDragPerTick);
        }
        if (!hasDrag) drag = 1.0;

        double finalDrag = drag;
        this.velocity = newVelocity
                // Apply gravity and drag
                .apply((x, y, z) -> new Vec(
                        x * finalDrag,
                        !hasNoGravity() ? (y - gravity) * gravityDrag : y,
                        z * finalDrag
                ))
                // Convert from block/tick to block/sec
                .mul(MinecraftServer.TICK_PER_SECOND)
                // Prevent infinitely decreasing velocity
                .apply(Vec.Operator.EPSILON);
    }

    @Override
    public void tick(long time) {
        final Pos posBefore = getPosition();
        super.tick(time);
        final Pos posNow = getPosition();

        Vec diff = Vec.fromPoint(posNow.sub(posBefore));
        PhysicsResult result = CollisionUtils.handlePhysics(
                instance, this.getChunk(),
                this.getBoundingBox(),
                posBefore, diff,
                null, true
        );

//        if (cooldown + 500 < System.currentTimeMillis()) {
//            float yaw = (float) Math.toDegrees(Math.atan2(diff.x(), diff.z()));
//            float pitch = (float) Math.toDegrees(Math.atan2(diff.y(), Math.sqrt(diff.x() * diff.x() + diff.z() * diff.z())));
//            super.refreshPosition(new Pos(posNow.x(), posNow.y(), posNow.z(), yaw, pitch));
//            cooldown = System.currentTimeMillis();
//        }

        PhysicsResult collided = CollisionUtils.checkEntityCollisions(instance, this.getBoundingBox(), posBefore, diff, 3, (e) -> e instanceof Player && e != shooter, result);
        if (collided != null && collided.collisionShapes()[0] != shooter) {
            if (collided.collisionShapes()[0] instanceof Player player) {
                collidePlayer(collided.newPosition(), player);

//                var e = new ProjectileCollideWithEntityEvent(this, collided.newPosition(), player);
//                MinecraftServer.getGlobalEventHandler().call(e);
                return;
            }
        }

        if (result.hasCollision()) {
            Block hitBlock = null;
            Point hitPoint = null;
            if (result.collisionShapes()[0] instanceof ShapeImpl block) {
                hitBlock = block.block();
                hitPoint = result.collisionPoints()[0];
            }
            if (result.collisionShapes()[1] instanceof ShapeImpl block) {
                hitBlock = block.block();
                hitPoint = result.collisionPoints()[1];
            }
            if (result.collisionShapes()[2] instanceof ShapeImpl block) {
                hitBlock = block.block();
                hitPoint = result.collisionPoints()[2];
            }

            if (hitBlock == null) return;

            collideBlock(hitPoint);

//            var e = new ProjectileCollideWithBlockEvent(this, Pos.fromPoint(hitPoint), hitBlock);
//            MinecraftServer.getGlobalEventHandler().call(e);
        }
    }

    public void setDrag(boolean drag) {
        this.hasDrag = drag;
    }

    public boolean hasDrag(boolean drag) {
        return this.hasDrag;
    }

    public void setGravityDrag(boolean drag) {
        this.hasGravityDrag = drag;
    }

    public boolean hasGravityDrag(boolean drag) {
        return this.hasGravityDrag;
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

    public boolean isTicking() {
        return this.ticking;
    }
}
