package dev.emortal.minestom.blocksumo.powerup.item.hook;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GrapplingHook extends PowerUp {
    private static final Component NAME = Component.text("Grappling Hook", NamedTextColor.GOLD);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.FISHING_ROD, NAME, ItemRarity.RARE);

    private final @NotNull FishingBobberManager bobberManager;

    public GrapplingHook(@NotNull BlockSumoGame game) {
        super(game, "grappling_hook", ITEM_INFO, SpawnLocation.CENTER);
        this.bobberManager = new FishingBobberManager(game);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        this.bobberManager.cast(player, hand);
    }

    @Override
    public boolean shouldRemoveEntityOnCollision() {
        return false;
    }

    @Override
    public void onCollideWithEntity(@NotNull EntityProjectile entity, @NotNull Player shooter, @NotNull Player target, @NotNull Pos collisionPos) {
        FishingBobber bobber = (FishingBobber) entity;
        if (!bobber.hasHooked()) {
            bobber.setHooked(target);
            this.bobberManager.setHooked(shooter, target);
        }
    }

    public void onRetract(@NotNull Player caster, @NotNull Player.Hand hand, @NotNull Pos bobberPos, @Nullable Player hooked) {
        this.removeOneItemFromPlayer(caster, hand);

        Pos casterPos = caster.getPosition();
        this.playRetractSound(casterPos);
        this.playRodBreakSound(caster);

        Vec gaming = bobberPos.sub(casterPos).asVec().normalize();
        caster.setVelocity(gaming.mul(25, 35, 25));
        if (hooked != null) {
            hooked.setVelocity(casterPos.sub(hooked.getPosition()).asVec().normalize().mul(25, 35, 25));
        }
    }

    private void playRetractSound(@NotNull Pos source) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE, Sound.Source.PLAYER, 1, 1);
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }

    private void playRodBreakSound(@NotNull Player caster) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_ITEM_BREAK, Sound.Source.PLAYER, 1, 1);
        Pos source = caster.getPosition();
        caster.playSound(sound, source.x(), source.y(), source.z());
    }
}
