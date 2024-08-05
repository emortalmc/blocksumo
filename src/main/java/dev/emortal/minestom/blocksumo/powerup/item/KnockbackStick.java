package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class KnockbackStick extends PowerUp {
    private static final Component NAME = Component.text("Zaza Stick", NamedTextColor.RED);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.STICK, NAME, ItemRarity.RARE);

    public KnockbackStick(@NotNull BlockSumoGame game) {
        super(game, "knockback_stick", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public void addExtraMetadata(@NotNull ItemStack.Builder builder) {
        builder.set(ItemComponent.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.KNOCKBACK, 1)));
    }

    @Override
    public void onAttack(@NotNull Player attacker, @NotNull Player victim) {
        this.removeOneItemFromPlayer(attacker, Player.Hand.MAIN);
        this.playHitSound(attacker);
    }

    private void playHitSound(@NotNull Player attacker) {
        attacker.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_BREAK, Sound.Source.PLAYER, 1, 1));
    }
}
