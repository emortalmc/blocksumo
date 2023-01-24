package dev.emortal.minestom.blocksumo.game.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class TNTRainEvent extends BlockSumoEvent {
    private static final Component START_MESSAGE = MiniMessage.miniMessage()
            .deserialize("<red>Uh oh... <gray>prepare for <italic>lots</italic> of explosions; <yellow>the TNT rain event just started");

    private static final Duration DURATION = Duration.ofSeconds(10);
    private static final Duration RATE = Duration.ofMillis(2500);

    private final Task task;

    public TNTRainEvent(@NotNull BlockSumoGame game) {
        super(game, START_MESSAGE, DURATION);

        this.task = MinecraftServer.getSchedulerManager().buildTask(this::spawnTnt)
                .repeat(RATE)
                .schedule();
    }

    private void spawnTnt() {
        BlockSumoInstance instance = this.game.getInstanceFuture().join();
        for (Player player : this.game.getPlayers()) {
            Pos tntPos = player.getPosition().add(0, 10, 0);

            Entity tntEntity = new Entity(EntityType.TNT);
            PrimedTntMeta meta = (PrimedTntMeta) tntEntity.getEntityMeta();
            meta.setFuseTime(80);

            tntEntity.setInstance(instance, tntPos);
            this.game.getAudience().playSound(Sound.sound(SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, 2, 1), tntEntity);
        }
    }

    @Override
    public void endEvent() {
        this.task.cancel();
        super.endEvent();
    }
}
