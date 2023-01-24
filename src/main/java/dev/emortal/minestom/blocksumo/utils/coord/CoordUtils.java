package dev.emortal.minestom.blocksumo.utils.coord;

import net.minestom.server.coordinate.Pos;

import java.util.Collection;

public class CoordUtils {

    public static Pos meanPos(Collection<Pos> positions) {
        double x = 0;
        double y = 0;
        double z = 0;
        for (Pos pos : positions) {
            x += pos.x();
            y += pos.y();
            z += pos.z();
        }
        return new Pos(x / positions.size(), y / positions.size(), z / positions.size());
    }
}
