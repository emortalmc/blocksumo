package dev.emortal.minestom.blocksumo.powerup.item.hook;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Thanks to
// https://github.com/Bloepiloepi/MinestomPvP/blob/master/src/main/java/io/github/bloepiloepi/pvp/projectile/FishingBobber.java
final class FishingBobber extends EntityProjectile {

    private final @NotNull FishingBobberManager manager;
    private final @NotNull Player caster;

    private @Nullable Player hooked;

    FishingBobber(@NotNull FishingBobberManager manager, @NotNull Player caster) {
        super(caster, EntityType.FISHING_BOBBER);
        this.manager = manager;
        this.caster = caster;
        this.setOwner(caster);
    }

    @Override
    public void update(long time) {
        if (this.shouldStopFishing(this.caster)) this.remove();
    }

    @Override
    public void remove() {
        super.remove();
        this.hooked = null;
        this.setOwner(null);
        this.manager.removeBobber(this.caster);
    }

    private boolean shouldStopFishing(@NotNull Player caster) {
        boolean holdingFishingRod = caster.getItemInMainHand().material() == Material.FISHING_ROD ||
                caster.getItemInOffHand().material() == Material.FISHING_ROD;
        if (caster.getGameMode() == GameMode.SPECTATOR || !holdingFishingRod) return true;

        if (this.hooked != null) {
            return this.hooked.isRemoved() || this.hooked.getGameMode() != GameMode.SURVIVAL;
        }
        return false;
    }

    boolean hasHooked() {
        return this.hooked != null;
    }

    void setHooked(@Nullable Player hooked) {
        this.meta().setHookedEntity(hooked);
        this.hooked = hooked;
    }

    private void setOwner(@Nullable Player owner) {
        this.meta().setOwnerEntity(owner);
    }

    private @NotNull FishingHookMeta meta() {
        return (FishingHookMeta) super.entityMeta;
    }
}
