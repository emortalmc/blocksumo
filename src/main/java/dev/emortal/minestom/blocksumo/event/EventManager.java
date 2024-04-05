package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.event.events.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.event.events.HotPotatoEvent;
import dev.emortal.minestom.blocksumo.event.events.MapClearEvent;
import dev.emortal.minestom.blocksumo.event.events.TntRainEvent;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class EventManager {

    private final @NotNull BlockSumoGame game;
    private final @NotNull RandomEventHandler randomEventHandler;

    private final @NotNull EventRegistry registry = new EventRegistry();

    public EventManager(@NotNull BlockSumoGame game) {
        this.game = game;
        this.randomEventHandler = new RandomEventHandler(game, this);
    }

    public void startRandomEventTask() {
        this.randomEventHandler.startRandomEventTask();
    }

    public void registerDefaultEvents() {
        this.registry.registerEvent("TNT_RAIN", TntRainEvent::new);
        this.registry.registerEvent("MAP_CLEAR", MapClearEvent::new);
        this.registry.registerEvent("HOT_POTATO", HotPotatoEvent::new);

        // April Fools
//        this.registry.registerEvent("BLOCK_RAIN", BlockRainEvent::new);
//        this.registry.registerEvent("SMALL_BORDER", SmallBorderEvent::new);

        // this can't be done for now because of a minestom bug.
        // see https://github.com/Minestom/Minestom/issues/289
//        this.registry.registerEvent("INSTA_BREAK", InstaBreakEvent::new);
//        this.registry.registerEvent("MOTHERLOAD", MotherloadEvent::new);
    }

    public void startEvent(@NotNull BlockSumoEvent event) {
        this.game.sendMessage(event.getStartMessage());
        this.game.playSound(
                Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_GROWL, Sound.Source.MASTER, 0.7f, 1.2f),
                Sound.Emitter.self()
        );
        event.start();
    }

    public @Nullable BlockSumoEvent findNamedEvent(@NotNull String eventName) {
        EventConstructor constructor = this.registry.findByName(eventName);
        if (constructor == null) return null;

        return constructor.create(this.game);
    }

    public @NotNull BlockSumoEvent findRandomEvent() {
        EventConstructor constructor = this.registry.findRandom();
        return constructor.create(this.game);
    }

    public @NotNull Collection<String> getEventNames() {
        return this.registry.getEventNames();
    }
}
