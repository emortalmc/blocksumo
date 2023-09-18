package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.explosion.ExplosionData;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public final class Tnt extends PowerUp {
    private static final Component NAME = Component.text("TNT", NamedTextColor.RED);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.TNT, NAME, ItemRarity.COMMON);

    private static final ExplosionData EXPLOSION = new ExplosionData(3, 35, 5.5, true);

    public Tnt(@NotNull BlockSumoGame game) {
        super(game, "tnt", ITEM_INFO, SpawnLocation.ANYWHERE);
    }

    @Override
    public boolean shouldHandleBlockPlace() {
        return true;
    }

    @Override
    public void onBlockPlace(@NotNull Player player, @NotNull Player.Hand hand, @NotNull Point clickedPos) {
        this.removeOneItemFromPlayer(player, hand);
        this.game.getExplosionManager().spawnTnt(clickedPos, 60, EXPLOSION, player);
    }
}
