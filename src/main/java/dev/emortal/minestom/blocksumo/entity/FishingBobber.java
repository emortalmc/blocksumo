package dev.emortal.minestom.blocksumo.entity;

import dev.emortal.minestom.blocksumo.game.PlayerTags;
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
public final class FishingBobber extends EntityProjectile {

    private final FishingBobberManager manager;
    private final Player caster;
    private Player hooked;

    public FishingBobber(@NotNull FishingBobberManager manager, @NotNull Player caster) {
        super(caster, EntityType.FISHING_BOBBER);
        this.manager = manager;
        this.caster = caster;
        setOwner(caster);
    }

    @Override
    public void update(long time) {
        if (shouldStopFishing(caster)) doRemove();
    }

    void doRemove() {
        remove();
        hooked = null;
        setOwner(null);
        manager.removeBobber(caster);
    }

    private boolean shouldStopFishing(@NotNull Player caster) {
        final boolean holdingFishingRod = caster.getItemInMainHand().material() == Material.FISHING_ROD ||
                caster.getItemInOffHand().material() == Material.FISHING_ROD;
        if (caster.getTag(PlayerTags.DEAD) || !holdingFishingRod) return true;

        if (hooked != null) {
            return hooked.isRemoved() || hooked.getGameMode() != GameMode.SURVIVAL;
        }
        return false;
    }

    public @Nullable Player getHooked() {
        return hooked;
    }

    public void setHooked(@Nullable Player hooked) {
        getMeta().setHookedEntity(hooked);
        this.hooked = hooked;
    }

    private void setOwner(@Nullable Player owner) {
        getMeta().setOwnerEntity(owner);
    }

    private @NotNull FishingHookMeta getMeta() {
        return (FishingHookMeta) entityMeta;
    }
}
