package dev.emortal.minestom.blocksumo.entity;

import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BetterEntityProjectile extends Entity {

    protected final @Nullable Player shooter;

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

        Collection<EntityCollisionResult> collisions = CollisionUtils.checkEntityCollisions(super.instance, super.getBoundingBox(), posBefore, diff, 3,
                entity -> entity instanceof Player player && player != this.shooter && player.getGameMode() != GameMode.SPECTATOR, result);
        EntityCollisionResult collided = !collisions.isEmpty() ? collisions.iterator().next() : null;

        Entity entity = !collisions.isEmpty() ? collided.entity() : null;
        if (entity != this.shooter) {
            if (entity instanceof Player player) {
                this.collidePlayer(collided.collisionPoint(), player);
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
        }
    }
}
