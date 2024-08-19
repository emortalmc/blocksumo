package dev.emortal.minestom.blocksumo.team;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class TeamColor {

    private static final NamedTextColor[] COLOURS = new NamedTextColor[] {
            NamedTextColor.BLACK,
            NamedTextColor.DARK_BLUE,
            NamedTextColor.DARK_GREEN,
            NamedTextColor.DARK_RED,
            NamedTextColor.DARK_PURPLE,
            NamedTextColor.GOLD,
            NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY,
            NamedTextColor.BLUE,
            NamedTextColor.GREEN,
            NamedTextColor.AQUA,
            NamedTextColor.DARK_AQUA,
            NamedTextColor.RED,
            NamedTextColor.LIGHT_PURPLE,
            NamedTextColor.YELLOW
    };
    private static final ItemStack[] ITEM_STACKS = new ItemStack[] {
            ItemStack.of(Material.BLACK_WOOL, 64),
            ItemStack.of(Material.BLUE_WOOL, 64),
            ItemStack.of(Material.GREEN_WOOL, 64),
            ItemStack.of(Material.RED_WOOL, 64),
            ItemStack.of(Material.PURPLE_WOOL, 64),
            ItemStack.of(Material.ORANGE_WOOL, 64),
            ItemStack.of(Material.LIGHT_GRAY_WOOL, 64),
            ItemStack.of(Material.GRAY_WOOL, 64),
            ItemStack.of(Material.LIGHT_BLUE_WOOL, 64),
            ItemStack.of(Material.LIME_WOOL, 64),
            ItemStack.of(Material.CYAN_WOOL, 64),
            ItemStack.of(Material.CYAN_WOOL, 64),
            ItemStack.of(Material.RED_WOOL, 64),
            ItemStack.of(Material.MAGENTA_WOOL, 64),
            ItemStack.of(Material.YELLOW_WOOL, 64)
    };

    private final RGBLike exactColor;
    private final NamedTextColor color;
    private final ItemStack item;

    public TeamColor(RGBLike exactColor) {
        this.exactColor = exactColor;
        NamedTextColor nearest = NamedTextColor.nearestTo(TextColor.color(exactColor));
        System.out.println(nearest);
        this.color = nearest;
        this.item = ITEM_STACKS[colourIndex(nearest)];
    }

    @NotNull
    public ItemStack getWoolItem() {
        return this.item;
    }

    public RGBLike getExactColor() {
        return exactColor;
    }
    public TextColor getTextColor() {
        return TextColor.color(exactColor);
    }

    public NamedTextColor getNamedTextColor() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeamColor otherCol)) return false;
        return getTextColor().compareTo(otherCol.getTextColor()) == 0;
    }

    private int colourIndex(NamedTextColor color) {
        for (int i = 0; i < COLOURS.length; i++) {
            if (COLOURS[i].equals(color)) {
                return i;
            }
        }
        return -1;
    }

}
