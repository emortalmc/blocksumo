package dev.emortal.minestom.blocksumo.game.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public abstract sealed class BlockSumoEvent permits TNTRainEvent {
    protected final @NotNull BlockSumoGame game;

    private final @NotNull Component startMessage;
    private final @NotNull Duration duration;

    private final @NotNull Task endTask;

    protected BlockSumoEvent(@NotNull BlockSumoGame game, @NotNull Component startMessage, @NotNull Duration duration) {
        this.game = game;
        this.startMessage = startMessage;
        this.duration = duration;

        this.endTask = MinecraftServer.getSchedulerManager().buildTask(this::endEvent)
                .delay(duration)
                .schedule();

        this.notifyStart();
    }

    protected void notifyStart() {
        Audience audience = this.game.getAudience();
        audience.sendMessage(this.startMessage);
        audience.playSound(
                Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_GROWL, Sound.Source.PLAYER, 0.7f, 1.2f),
                Sound.Emitter.self()
        );
    }

    public void endEvent() {
        this.endTask.cancel();
    }

    public @NotNull Duration getDuration() {
        return this.duration;
    }

    public @NotNull Component getStartMessage() {
        return this.startMessage;
    }
}
