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
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GrapplingHook extends PowerUp {

    private static final float MAX_VELOCITY = 0.4F;

    private static final Component NAME = Component.text("Grappling Hook", NamedTextColor.GOLD);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.FISHING_ROD, NAME, ItemRarity.RARE);

    public static final Map<UUID, FishingBobber> PLAYER_BOBBER_MAP = new HashMap<>();

    public GrapplingHook(@NotNull BlockSumoGame game) {
        super(game, "grappling_hook", ITEM_INFO, SpawnLocation.CENTER);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        if (PLAYER_BOBBER_MAP.containsKey(player.getUuid())) { // Player has already cast bobber, we must retract it
            retract(player, hand);
            return;
        }

        FishingBobber bobber = createBobber(player, calculateCastPos(player));
        PLAYER_BOBBER_MAP.put(player.getUuid(), bobber);
    }

    private @NotNull Pos calculateCastPos(@NotNull Player caster) {
        float yaw = caster.getPosition().yaw();

        double directionX = Math.sin(Math.toRadians(-yaw) - Math.PI);
        double directionZ = Math.cos(Math.toRadians(-yaw) - Math.PI);

        double x = caster.getPosition().x() - directionX * 0.3;
        double y = caster.getPosition().y() + caster.getEyeHeight();
        double z = caster.getPosition().z() - directionZ * 0.3;
        return new Pos(x, y, z);
    }

    private @NotNull FishingBobber createBobber(@NotNull Player caster, @NotNull Pos castPos) {
        FishingBobber bobber = new FishingBobber(caster);

        bobber.setTag(PowerUp.NAME, "grappling_hook");
        bobber.setInstance(this.game.getInstance(), castPos);
        bobber.setVelocity(caster.getPosition().direction().normalize().mul(45));

        return bobber;
    }

    void retract(@NotNull Player caster, @NotNull Player.Hand hand) {
        FishingBobber bobber = GrapplingHook.PLAYER_BOBBER_MAP.get(caster.getUuid());
        if (bobber == null) return;

        PowerUp heldPowerUp = this.game.getPowerUpManager().getHeldPowerUp(caster, hand);
        if (heldPowerUp instanceof GrapplingHook hook) {
            hook.onRetract(caster, hand, bobber.getPosition(), bobber.getHooked());
        }

        bobber.remove();
        GrapplingHook.PLAYER_BOBBER_MAP.remove(caster.getUuid());
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
