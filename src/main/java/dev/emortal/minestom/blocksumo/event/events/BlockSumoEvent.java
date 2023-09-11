package dev.emortal.minestom.blocksumo.event.events;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public sealed interface BlockSumoEvent permits MapClearEvent, MotherloadEvent, TNTRainEvent {

    void start();

    @NotNull Component getStartMessage();
}
