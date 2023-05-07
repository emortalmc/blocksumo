package dev.emortal.minestom.blocksumo.utils.raycast;

import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.area.area3d.Area3dRectangularPrism;
import dev.emortal.rayfast.casting.grid.GridCast;
import dev.emortal.rayfast.vector.Vector3d;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RaycastUtil {

    private static final Map<BoundingBox, Area3d> boundingBoxToArea3d = new ConcurrentHashMap<>();

    static {
        Area3d.CONVERTER.register(BoundingBox.class, box -> {
            return boundingBoxToArea3d.computeIfAbsent(box, key -> Area3dRectangularPrism.wrapper(key,
                    b -> b.minX() - 0.5, b -> b.minY() - 0.5, b -> b.minZ() - 0.5,
                    b -> b.maxX() + 0.5, b -> b.maxY() + 0.5, b -> b.maxZ() + 0.5
            ));
        });
    }

    public static @NotNull RaycastResult raycast(@NotNull RaycastContext context) {
        final Pos blockRaycast = raycastBlock(context);
        final EntityResult entityRaycast = raycastEntity(context);

        if (entityRaycast == null && blockRaycast == null) return new RaycastResult(RaycastResultType.MISS, null, null);
        if (entityRaycast == null) return new RaycastResult(RaycastResultType.HIT_BLOCK, null, blockRaycast);
        if (blockRaycast == null) return new RaycastResult(RaycastResultType.HIT_ENTITY, entityRaycast.entity(), entityRaycast.pos());

        // Both entity and block check have collided, we need to determine which is closer

        final double distanceFromEntity = context.start().distanceSquared(entityRaycast.pos());
        final double distanceFromBlock = context.start().distanceSquared(blockRaycast);

        if (distanceFromBlock > distanceFromEntity) {
            return new RaycastResult(RaycastResultType.HIT_ENTITY, entityRaycast.entity(), entityRaycast.pos());
        } else {
            return new RaycastResult(RaycastResultType.HIT_BLOCK, null, blockRaycast);
        }
    }

    private static @Nullable Pos raycastBlock(@NotNull RaycastContext context) {
        final Point start = context.start();
        final Vec direction = context.direction();
        final Iterator<Vector3d> gridIterator = GridCast.createExactGridIterator(
                start.x(), start.y(), start.z(),
                direction.x(), direction.y(), direction.z(),
                1, context.maxDistance()
        );

        while (gridIterator.hasNext()) {
            final Vector3d gridUnit = gridIterator.next();
            final Pos pos = new Pos(gridUnit.get(0), gridUnit.get(1), gridUnit.get(2));

            try {
                final Block hitBlock = context.instance().getBlock(pos, Block.Getter.Condition.TYPE);
                if (hitBlock.isSolid()) return pos;
            } catch (final NullPointerException exception) {
                // Catch if chunk is not loaded
                break;
            }
        }
        return null;
    }

    private static @Nullable EntityResult raycastEntity(@NotNull RaycastContext context) {
        final Instance instance = context.instance();
        final Point start = context.start();
        final Vec direction = context.direction();
        final double maxDistance = context.maxDistance();

        for (final Entity entity : instance.getEntities()) {
            if (!context.entityHitPredicate().test(entity)) continue;

            final Pos pos = entity.getPosition();
            if (pos.distanceSquared(start) > maxDistance * maxDistance) continue;

            final Area3d area = getEntityArea(entity);
            final Vector3d intersection = area.lineIntersection(
                    Vector3d.of(start.x() - pos.x(), start.y() - pos.y(), start.z() - pos.z()),
                    Vector3d.of(direction.x(), direction.y(), direction.z())
            );
            if (intersection != null) {
                final double intersectX = intersection.get(0) + pos.x();
                final double intersectY = intersection.get(1) + pos.y();
                final double intersectZ = intersection.get(2) + pos.z();
                return new EntityResult(entity, new Pos(intersectX, intersectY, intersectZ));
            }
        }
        return null;
    }

    private static @NotNull Area3d getEntityArea(@NotNull Entity entity) {
        return Area3d.CONVERTER.from(entity.getBoundingBox());
    }

    private RaycastUtil() {
        throw new AssertionError("This class cannot be instantiated.");
    }

    private record EntityResult(@NotNull Entity entity, @NotNull Pos pos) {
    }
}
