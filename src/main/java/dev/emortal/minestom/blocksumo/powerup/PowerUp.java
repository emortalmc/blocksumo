package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public abstract class PowerUp {
    public static final Tag<String> NAME = Tag.String("power_up");

    protected final BlockSumoGame game;
    protected final String name;
    private final PowerUpItemInfo itemInfo;
    private final SpawnLocation spawnLocation;

    public PowerUp(@NotNull BlockSumoGame game, @NotNull String name, @NotNull PowerUpItemInfo itemInfo, @NotNull SpawnLocation spawnLocation) {
        this.game = game;
        this.name = name;
        this.itemInfo = itemInfo;
        this.spawnLocation = spawnLocation;
    }

    public void addExtraMetadata(@NotNull ItemMeta.Builder builder) {
        // Do nothing by default
    }

    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        // Do nothing by default
    }

    public void onUseOnBlock(@NotNull Player player, @NotNull Player.Hand hand) {
        // Do nothing by default
    }

    public void onAttack(@NotNull Player attacker, @NotNull Player victim) {
        // Do nothing by default
    }

    public void onCollide(@NotNull Player shooter, @NotNull Pos collisionPosition) {
        // Do nothing by default
    }

    public void onBlockPlace(@NotNull PlayerBlockPlaceEvent event) {
        // Do nothing by default
    }

    protected final void removeOneItemFromPlayer(@NotNull Player player, @NotNull Player.Hand hand) {
        final ItemStack heldItem = player.getItemInHand(hand);

        final ItemStack newHeldItem;
        if (heldItem.amount() == 1) {
            newHeldItem = ItemStack.AIR;
        } else {
            newHeldItem = heldItem.withAmount(heldItem.amount() - 1);
        }
        player.setItemInHand(hand, newHeldItem);
    }

    public final @NotNull ItemStack createItemStack() {
        return ItemStack.builder(itemInfo.material())
                .amount(itemInfo.amount())
                .meta(builder -> {
                    builder.displayName(itemInfo.name().decoration(TextDecoration.ITALIC, false));
                    builder.lore(itemInfo.rarity().getName().decoration(TextDecoration.ITALIC, false));
                    addExtraMetadata(builder);
                    builder.setTag(NAME, name);
                })
                .build();
    }

    public @NotNull String getName() {
        return name;
    }
}
