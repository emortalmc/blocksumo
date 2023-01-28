package dev.emortal.minestom.blocksumo.powerup;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public record PowerUpItemInfo(@NotNull Material material, @NotNull Component name, @NotNull ItemRarity rarity, int amount) {

    public PowerUpItemInfo(@NotNull Material material, @NotNull Component name, @NotNull ItemRarity rarity) {
        this(material, name, rarity, 1);
    }
}
