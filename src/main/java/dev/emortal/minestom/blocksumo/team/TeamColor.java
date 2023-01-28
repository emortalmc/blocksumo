package dev.emortal.minestom.blocksumo.team;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public enum TeamColor {

    BLACK(NamedTextColor.BLACK, Block.BLACK_WOOL, Material.BLACK_WOOL),
    DARK_BLUE(NamedTextColor.DARK_BLUE, Block.BLUE_WOOL, Material.BLUE_WOOL),
    DARK_GREEN(NamedTextColor.DARK_GREEN, Block.GREEN_WOOL, Material.GREEN_WOOL),
    DARK_RED(NamedTextColor.DARK_RED, Block.RED_WOOL, Material.RED_WOOL),
    DARK_PURPLE(NamedTextColor.DARK_PURPLE, Block.PURPLE_WOOL, Material.PURPLE_WOOL),
    GOLD(NamedTextColor.GOLD, Block.ORANGE_WOOL, Material.ORANGE_WOOL),
    GRAY(NamedTextColor.GRAY, Block.LIGHT_GRAY_WOOL, Material.LIGHT_GRAY_WOOL),
    DARK_GRAY(NamedTextColor.DARK_GRAY, Block.GRAY_WOOL, Material.GRAY_WOOL),
    BLUE(NamedTextColor.BLUE, Block.LIGHT_BLUE_WOOL, Material.LIGHT_BLUE_WOOL),
    GREEN(NamedTextColor.GREEN, Block.LIME_WOOL, Material.LIME_WOOL),
    AQUA(NamedTextColor.AQUA, Block.CYAN_WOOL, Material.CYAN_WOOL),
    RED(NamedTextColor.RED, Block.RED_WOOL, Material.RED_WOOL),
    LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE, Block.MAGENTA_WOOL, Material.MAGENTA_WOOL),
    YELLOW(NamedTextColor.YELLOW, Block.YELLOW_WOOL, Material.YELLOW_WOOL);

    private final RGBLike color;
    private final Block woolBlock;
    private final Material woolMaterial;

    TeamColor(RGBLike color, Block woolBlock, Material woolMaterial) {
        this.color = color;
        this.woolBlock = woolBlock;
        this.woolMaterial = woolMaterial;
    }

    public @NotNull RGBLike getColor() {
        return color;
    }

    public @NotNull Block getWoolBlock() {
        return woolBlock;
    }

    public @NotNull Material getWoolMaterial() {
        return woolMaterial;
    }
}
