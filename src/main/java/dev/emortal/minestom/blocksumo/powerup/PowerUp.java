package dev.emortal.minestom.blocksumo.powerup;

import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public abstract class PowerUp {
    public static final Tag<String> ID = Tag.String("power_up_id");

    private final String id;
    private final PowerUpItemInfo itemInfo;
    private final SpawnLocation spawnLocation;

    public PowerUp(@NotNull String id, @NotNull PowerUpItemInfo itemInfo, @NotNull SpawnLocation spawnLocation) {
        this.id = id;
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

    public final @NotNull ItemStack createItemStack() {
        return ItemStack.builder(itemInfo.material())
                .amount(itemInfo.amount())
                .meta(builder -> {
                    builder.displayName(itemInfo.name().decoration(TextDecoration.ITALIC, false));
                    builder.lore(itemInfo.rarity().getName().decoration(TextDecoration.ITALIC, false));
                    addExtraMetadata(builder);
                    builder.setTag(ID, id);
                })
                .build();
    }
}
