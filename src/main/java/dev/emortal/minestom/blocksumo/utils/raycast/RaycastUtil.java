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
        Area3d.CONVERTER.register(BoundingBox.class, box -> boundingBoxToArea3d.computeIfAbsent(box, RaycastUtil::convertBoundingBox));
    }

    private static @NotNull Area3d convertBoundingBox(@NotNull BoundingBox box) {
        return Area3dRectangularPrism.wrapper(box,
                b -> b.minX() - 0.5, b -> b.minY() - 0.5, b -> b.minZ() - 0.5,
                b -> b.maxX() + 0.5, b -> b.maxY() + 0.5, b -> b.maxZ() + 0.5);
    }

    public static @NotNull RaycastResult raycast(@NotNull RaycastContext context) {
        Pos blockRaycast = raycastBlock(context);
        EntityResult entityRaycast = raycastEntity(context);

        if (entityRaycast == null && blockRaycast == null) {
            return new RaycastResult(RaycastResultType.MISS, null, null);
        }
        if (entityRaycast == null) {
            return new RaycastResult(RaycastResultType.HIT_BLOCK, null, blockRaycast);
        }
        if (blockRaycast == null) {
            return new RaycastResult(RaycastResultType.HIT_ENTITY, entityRaycast.entity(), entityRaycast.pos());
        }

        // Both entity and block check have collided, we need to determine which is closer

        double distanceFromEntity = context.start().distanceSquared(entityRaycast.pos());
        double distanceFromBlock = context.start().distanceSquared(blockRaycast);

        if (distanceFromBlock > distanceFromEntity) {
            return new RaycastResult(RaycastResultType.HIT_ENTITY, entityRaycast.entity(), entityRaycast.pos());
        } else {
            return new RaycastResult(RaycastResultType.HIT_BLOCK, null, blockRaycast);
        }
    }

    private static @Nullable Pos raycastBlock(@NotNull RaycastContext context) {
        Point start = context.start();
        Vec direction = context.direction();

        Iterator<Vector3d> gridIterator = GridCast.createExactGridIterator(
                start.x(), start.y(), start.z(),
                direction.x(), direction.y(), direction.z(),
                1, context.maxDistance()
        );

        while (gridIterator.hasNext()) {
            Vector3d gridUnit = gridIterator.next();
            Pos pos = new Pos(gridUnit.get(0), gridUnit.get(1), gridUnit.get(2));

            try {
                Block hitBlock = context.instance().getBlock(pos, Block.Getter.Condition.TYPE);
                if (hitBlock.isSolid()) return pos;
            } catch (NullPointerException exception) {
                // Catch if chunk is not loaded
                break;
            }
        }
        return null;
    }

    private static @Nullable EntityResult raycastEntity(@NotNull RaycastContext context) {
        Instance instance = context.instance();
        Point start = context.start();
        Vec direction = context.direction();
        double maxDistance = context.maxDistance();

        for (Entity entity : instance.getEntities()) {
            if (!context.entityHitPredicate().test(entity)) continue;

            Pos pos = entity.getPosition();
            if (pos.distanceSquared(start) > maxDistance * maxDistance) continue;

            Area3d area = getEntityArea(entity);
            Vector3d intersection = area.lineIntersection(
                    Vector3d.of(start.x() - pos.x(), start.y() - pos.y(), start.z() - pos.z()),
                    Vector3d.of(direction.x(), direction.y(), direction.z())
            );

            if (intersection != null) {
                double intersectX = intersection.get(0) + pos.x();
                double intersectY = intersection.get(1) + pos.y();
                double intersectZ = intersection.get(2) + pos.z();
                return new EntityResult(entity, new Pos(intersectX, intersectY, intersectZ));
            }
        }
        return null;
    }

    private static @NotNull Area3d getEntityArea(@NotNull Entity entity) {
        return Area3d.CONVERTER.from(entity.getBoundingBox());
    }

    private RaycastUtil() {
    }

    private record EntityResult(@NotNull Entity entity, @NotNull Pos pos) {
    }
}
