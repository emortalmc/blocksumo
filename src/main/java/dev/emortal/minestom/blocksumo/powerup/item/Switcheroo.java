package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import dev.emortal.minestom.blocksumo.utils.raycast.*;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Switcheroo extends PowerUp {
    private static final Component NAME = MiniMessage.miniMessage().deserialize("<rainbow>Switcheroo</rainbow>");
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.ENDER_EYE, NAME, ItemRarity.LEGENDARY);

    public Switcheroo(@NotNull BlockSumoGame game) {
        super(game, "switcheroo", ITEM_INFO, SpawnLocation.CENTER);
    }

    @Override
    public void onUse(@NotNull Player player, Player.@NotNull Hand hand) {
        this.removeOneItemFromPlayer(player, hand);
        this.playSwitchSound(player);

        Vec eyePosition = player.getPosition().add(0, player.getEyeHeight(), 0).asVec();
        RaycastResult raycast = doRaycast(eyePosition, player);
        Point hitPos = raycast.hitPosition();

        this.showSwitchParticle(player, hitPos == null ? null : Vec.fromPoint(hitPos), eyePosition);

        if (raycast.type() == RaycastResultType.HIT_ENTITY) {
            this.doSwitcheroo(player, Objects.requireNonNull(raycast.hitEntity()));
        }
    }

    private void playSwitchSound(@NotNull Player player) {
        Sound sound = Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.PLAYER, 1, 1);
        Pos source = player.getPosition();
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }

    private @NotNull RaycastResult doRaycast(@NotNull Point eyePosition, @NotNull Player player) {
        Vec direction = player.getPosition().direction();
        EntityHitPredicate predicate = entity ->
                entity instanceof Player other &&
                    other.getGameMode() == GameMode.SURVIVAL &&
                    other != player;

        RaycastContext context = new RaycastContext(this.game.getInstance(), eyePosition, direction, 60, predicate);
        return RaycastUtil.raycast(context);
    }

    private void showSwitchParticle(@NotNull Player player, @Nullable Vec hitPos, @NotNull Vec eyePosition) {
        Vec targetPos = hitPos != null ? hitPos : eyePosition.add(player.getPosition().direction().mul(20));

        double step = 0.1;
        Vec direction = targetPos.sub(eyePosition).normalize().mul(step);

        Vec currentPos = eyePosition;
        for (int i = 0; i < eyePosition.distance(targetPos) * (1.0 / step); i++) {
            currentPos = currentPos.add(direction);

            ParticlePacket packet = new ParticlePacket(Particle.END_ROD, true, currentPos.x(), currentPos.y(), currentPos.z(), 0f, 0f, 0f, 0, 1);
            this.game.sendGroupedPacket(packet);
        }
    }

    private void doSwitcheroo(@NotNull Player player, @NotNull Entity target) {
        Pos targetPos = target.getPosition();
        Vec targetVelocity = target.getVelocity();

        Pos playerPos = player.getPosition();
        Vec playerVelocity = player.getVelocity();

        target.teleport(playerPos);
        target.setVelocity(playerVelocity);

        player.teleport(targetPos);
        player.setVelocity(targetVelocity);

        this.playTeleportSound(playerPos);
        this.playTeleportSound(targetPos);
    }

    private void playTeleportSound(@NotNull Point source) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Sound.Source.PLAYER, 1, 1);
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }
}
