package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class ExtraLife extends PowerUp {
    private static final Component NAME = MiniMessage.miniMessage().deserialize("<rainbow>Extra Life</rainbow>");
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.BEACON, NAME, ItemRarity.LEGENDARY);

    public ExtraLife(@NotNull BlockSumoGame game) {
        super(game, "extralife", ITEM_INFO, SpawnLocation.CENTER);
    }

    @Override
    public boolean shouldHandleBlockPlace() {
        return true;
    }

    @Override
    public void onUse(@NotNull Player player, Player.@NotNull Hand hand) {
        this.removeOneItemFromPlayer(player, hand);
        this.playExtraLifeSound(player);

        byte prevLives = player.getTag(PlayerTags.LIVES);
        player.setTag(PlayerTags.LIVES, (byte)(prevLives + 1));
        this.game.getPlayerManager().updateRemainingLives(player, prevLives + 1);
    }

    private void playExtraLifeSound(@NotNull Player player) {
        Sound sound = Sound.sound(SoundEvent.BLOCK_BEACON_POWER_SELECT, Sound.Source.PLAYER, 1, 1);
        Pos source = player.getPosition();
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }
}
