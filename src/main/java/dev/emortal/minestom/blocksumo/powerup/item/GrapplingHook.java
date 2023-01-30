package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.entity.FishingBobber;
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

    public GrapplingHook(@NotNull BlockSumoGame game) {
        super(game, "grappling_hook", ITEM_INFO, SpawnLocation.CENTER);
    }

    @Override
    public boolean shouldRemoveEntityOnCollision() {
        return false;
    }

    @Override
    public void onCollideWithEntity(@NotNull EntityProjectile entity, @NotNull Player shooter, @NotNull Player target,
                                    @NotNull Pos collisionPos) {
        final FishingBobber bobber = (FishingBobber) entity;
        if (bobber.getHooked() == null) {
            bobber.setHooked(target);
            game.getBobberManager().setHooked(shooter, target);
        }
    }

    public void onRetract(@NotNull Player caster, @NotNull Player.Hand hand, @NotNull Pos bobberPos, @Nullable Player hooked) {
        removeOneItemFromPlayer(caster, hand);

        final Pos casterPos = caster.getPosition();
        playRetractSound(casterPos);
        playRodBreakSound(caster);

        final Vec gaming = bobberPos.sub(casterPos).asVec().normalize();
        caster.setVelocity(gaming.mul(25, 35, 25));
        if (hooked != null) {
            hooked.setVelocity(casterPos.sub(hooked.getPosition()).asVec().normalize().mul(25, 35, 25));
        }
    }

    private void playRetractSound(@NotNull Pos source) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE, Sound.Source.PLAYER, 1, 1);
        game.getAudience().playSound(sound, source.x(), source.y(), source.z());
    }

    private void playRodBreakSound(@NotNull Player caster) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_ITEM_BREAK, Sound.Source.PLAYER, 1, 1);
        final Pos source = caster.getPosition();
        caster.playSound(sound, source.x(), source.y(), source.z());
    }
}
