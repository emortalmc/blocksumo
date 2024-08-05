package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InstaBreakEvent implements BlockSumoEvent {
    private static final Component START_MESSAGE = MiniMessage.miniMessage()
            .deserialize("<red>Feel the surge of empowerment! <yellow>With a mere touch, blocks crumble <i>effortlessly!</yellow>");

    private static final ItemStack ENCHANTED_SHEARS = ItemStack.builder(Material.SHEARS)
            .set(ItemComponent.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.EFFICIENCY, 4)))
            .build();

    private final @NotNull BlockSumoGame game;

    public InstaBreakEvent(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    @Override
    public void start() {
        for (Player player : this.game.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;

            this.replaceShearsItem(player, ENCHANTED_SHEARS);
        }
    }

    private void end() {
        for (Player player : this.game.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;

            PlayerInventory inventory = player.getInventory();
            if (inventory.getCursorItem().material().equals(Material.SHEARS)) inventory.setCursorItem(ItemStack.of(Material.SHEARS));
        }
    }

    @Override
    public @NotNull Component getStartMessage() {
        return START_MESSAGE;
    }

    private void replaceShearsItem(@NotNull Player player, @NotNull ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();
        if (inventory.getCursorItem().material().equals(Material.SHEARS)) inventory.setCursorItem(itemStack);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItemStack(i);

            if (stack.material().equals(Material.SHEARS)) {
                inventory.setItemStack(i, itemStack);
                return;
            }
        }
    }
}
