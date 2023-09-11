package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class Puncher extends PowerUp {
    private static final PowerUpItemInfo ITEM_INFO =
            new PowerUpItemInfo(Material.BLAZE_ROD, Component.text("Puncher", NamedTextColor.RED), ItemRarity.RARE);

    public Puncher(@NotNull BlockSumoGame game) {
        super(game, "puncher", ITEM_INFO, SpawnLocation.CENTER);
    }

    @Override
    public void addExtraMetadata(@NotNull ItemMeta.Builder builder) {
        builder.enchantment(Enchantment.KNOCKBACK, (short) 4);
    }

    @Override
    public void onAttack(@NotNull Player attacker, @NotNull Player victim) {
        this.removeOneItemFromPlayer(attacker, Player.Hand.MAIN);
        this.playHitSound(attacker);
    }

    private void playHitSound(@NotNull Player player) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, Sound.Source.PLAYER, 1, 1);
        Pos source = player.getPosition();
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }
}
