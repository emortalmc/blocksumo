package dev.emortal.minestom.blocksumo.event.events;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface BlockSumoEvent {

    void start();

    @NotNull Component getStartMessage();
}
