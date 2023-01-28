package dev.emortal.minestom.blocksumo.powerup;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum ItemRarity {

    COMMON(Component.text("COMMON", NamedTextColor.GRAY, TextDecoration.BOLD), 15),
    UNCOMMON(Component.text("UNCOMMON", NamedTextColor.GREEN, TextDecoration.BOLD), 8),
    RARE(Component.text("RARE", NamedTextColor.AQUA, TextDecoration.BOLD), 5),
    LEGENDARY(MiniMessage.miniMessage().deserialize("<bold><gradient:light_purple:gold>LEGENDARY</gradient></bold>"), 1),
    IMPOSSIBLE(Component.empty(), 0);

    private final Component name;
    private final int weight;

    ItemRarity(Component name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public Component getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }
}
