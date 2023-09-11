package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.explosion.ExplosionData;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class AntiGravityTNT extends PowerUp {
    private static final Component NAME = Component.text("Anti-Gravity TNT", NamedTextColor.AQUA);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.CYAN_CONCRETE_POWDER, NAME, ItemRarity.UNCOMMON);

    private static final ExplosionData EXPLOSION = new ExplosionData(3, 35, 5.5, true);

    public AntiGravityTNT(@NotNull BlockSumoGame game) {
        super(game, "anti_gravity_tnt", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public boolean shouldHandleBlockPlace() {
        return true;
    }

    @Override
    public void onBlockPlace(@NotNull Player player, @NotNull Player.Hand hand, @NotNull Point clickedPos) {
        this.removeOneItemFromPlayer(player, hand);

        Entity tnt = this.game.getExplosionManager().spawnTnt(clickedPos.sub(0, 0.4, 0), 60, EXPLOSION, player);
        tnt.setNoGravity(true);

        this.playPrimedSound(clickedPos);
        tnt.scheduler().buildTask(() -> this.setNewUpwardsVelocity(tnt)).repeat(TaskSchedule.nextTick()).schedule();
    }

    private void playPrimedSound(@NotNull Point source) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, 2, 1);
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }

    private void setNewUpwardsVelocity(@NotNull Entity tnt) {
        Vec velocity = tnt.getPosition().y() > 80 ? Vec.ZERO : new Vec(0, 7, 0);
        tnt.setVelocity(velocity);
    }
}
