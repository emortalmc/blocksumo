package dev.emortal.minestom.blocksumo.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class BetterEntity extends Entity {

    private boolean ticking = true;

    public BetterEntity(@NotNull EntityType entityType) {
        super(entityType);
    }

    @Override
    public void tick(long time) {
        if (this.ticking) super.tick(time);
    }

    public void setPhysics(boolean physics) {
        this.hasPhysics = physics;
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }
}
