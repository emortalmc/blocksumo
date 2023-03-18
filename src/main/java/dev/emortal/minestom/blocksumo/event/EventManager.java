package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.event.events.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.event.events.MapClearEvent;
import dev.emortal.minestom.blocksumo.event.events.MotherloadEvent;
import dev.emortal.minestom.blocksumo.event.events.TNTRainEvent;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class EventManager {

    private final EventRegistry registry;
    private final BlockSumoGame game;
    private final RandomEventHandler randomEventHandler;

    public EventManager(final @NotNull BlockSumoGame game) {
        this.registry = new EventRegistry();
        this.game = game;
        this.randomEventHandler = new RandomEventHandler(game, this);
    }

    public void startRandomEventTask() {
        randomEventHandler.startRandomEventTask();
    }

    public void registerDefaultEvents() {
        registry.registerEvent("TNT_RAIN", TNTRainEvent::new);
        registry.registerEvent("MAP_CLEAR", MapClearEvent::new);
        registry.registerEvent("MOTHERLOAD", MotherloadEvent::new);
    }

    public void startEvent(final @NotNull BlockSumoEvent event) {
        final Audience audience = game.getAudience();
        audience.sendMessage(event.getStartMessage());
        audience.playSound(
                Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_GROWL, Sound.Source.MASTER, 0.7f, 1.2f),
                Sound.Emitter.self()
        );
        event.start();
    }

    public @Nullable BlockSumoEvent findNamedEvent(final @NotNull String eventName) {
        final EventConstructor constructor = registry.findByName(eventName);
        if (constructor == null) return null;
        return constructor.create(game);
    }

    public @NotNull BlockSumoEvent findRandomEvent() {
        final EventConstructor constructor = registry.findRandom();
        return constructor.create(game);
    }

    public @NotNull Collection<String> getEventNames() {
        return registry.getEventNames();
    }
}
