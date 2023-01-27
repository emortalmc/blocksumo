package dev.emortal.minestom.blocksumo.game.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public abstract sealed class BlockSumoEvent permits TNTRainEvent {
    protected final @NotNull BlockSumoGame game;
    private final @NotNull Component startMessage;

    protected BlockSumoEvent(@NotNull BlockSumoGame game, @NotNull Component startMessage) {
        this.game = game;
        this.startMessage = startMessage;
    }

    protected void notifyStart() {
        Audience audience = this.game.getAudience();
        audience.sendMessage(this.startMessage);
        audience.playSound(
                Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_GROWL, Sound.Source.MASTER, 0.7f, 1.2f),
                Sound.Emitter.self()
        );
    }

    public void start() {
        notifyStart();
        run();
    }

    protected abstract void run();

    public @NotNull Component getStartMessage() {
        return this.startMessage;
    }
}
