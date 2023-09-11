package dev.emortal.minestom.blocksumo.team;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public enum TeamColor {

    BLACK(NamedTextColor.BLACK, Material.BLACK_WOOL),
    DARK_BLUE(NamedTextColor.DARK_BLUE, Material.BLUE_WOOL),
    DARK_GREEN(NamedTextColor.DARK_GREEN, Material.GREEN_WOOL),
    DARK_RED(NamedTextColor.DARK_RED, Material.RED_WOOL),
    DARK_PURPLE(NamedTextColor.DARK_PURPLE, Material.PURPLE_WOOL),
    GOLD(NamedTextColor.GOLD, Material.ORANGE_WOOL),
    GRAY(NamedTextColor.GRAY, Material.LIGHT_GRAY_WOOL),
    DARK_GRAY(NamedTextColor.DARK_GRAY, Material.GRAY_WOOL),
    BLUE(NamedTextColor.BLUE, Material.LIGHT_BLUE_WOOL),
    GREEN(NamedTextColor.GREEN, Material.LIME_WOOL),
    AQUA(NamedTextColor.AQUA, Material.CYAN_WOOL),
    RED(NamedTextColor.RED, Material.RED_WOOL),
    LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_WOOL),
    YELLOW(NamedTextColor.YELLOW, Material.YELLOW_WOOL);

    private final @NotNull TextColor color;
    private final @NotNull ItemStack woolItem;

    TeamColor(@NotNull TextColor color, @NotNull Material woolItem) {
        this.color = color;
        this.woolItem = ItemStack.of(woolItem, 64);
    }

    public @NotNull TextColor getColor() {
        return this.color;
    }

    public @NotNull ItemStack getWoolItem() {
        return this.woolItem;
    }
}
