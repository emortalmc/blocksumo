package dev.emortal.minestom.blocksumo.powerup;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PowerUp {
    public static final @NotNull Tag<String> NAME = Tag.String("power_up");

    protected final @NotNull BlockSumoGame game;
    protected final @NotNull String name;
    private final @NotNull PowerUpItemInfo itemInfo;
    private final @NotNull SpawnLocation spawnLocation;

    protected PowerUp(@NotNull BlockSumoGame game, @NotNull String name, @NotNull PowerUpItemInfo itemInfo,
                      @NotNull SpawnLocation spawnLocation) {
        this.game = game;
        this.name = name;
        this.itemInfo = itemInfo;
        this.spawnLocation = spawnLocation;
    }

    public void addExtraMetadata(@NotNull ItemStack.Builder builder) {
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

    public boolean shouldHandleBlockPlace() {
        return false;
    }

    public void onBlockPlace(@NotNull Player player, @NotNull Player.Hand hand, @NotNull Point clickedPos) {
        // Do nothing by default
    }

    protected final void removeOneItemFromPlayer(@NotNull Player player, @NotNull Player.Hand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        ItemStack newHeldItem;
        if (heldItem.amount() == 1) {
            newHeldItem = ItemStack.AIR;
        } else {
            newHeldItem = heldItem.withAmount(heldItem.amount() - 1);
        }
        player.setItemInHand(hand, newHeldItem);
    }

    public final @NotNull ItemStack createItemStack() {
        ItemStack.Builder builder = ItemStack.builder(this.itemInfo.material())
                .amount(this.itemInfo.amount())
                .set(ItemComponent.ITEM_NAME, this.itemInfo.name())
                .set(ItemComponent.LORE, List.of(this.itemInfo.rarity().getName()))
                .set(NAME, this.name);

        this.addExtraMetadata(builder);

        return builder.build();
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull Component getItemName() {
        return this.itemInfo.name();
    }

    public @NotNull ItemRarity getRarity() {
        return this.itemInfo.rarity();
    }

    public @NotNull SpawnLocation getSpawnLocation() {
        return this.spawnLocation;
    }
}
