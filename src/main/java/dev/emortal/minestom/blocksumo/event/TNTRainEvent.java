package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapData;
import java.util.function.Supplier;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class TNTRainEvent extends BlockSumoEvent {
    private static final Component START_MESSAGE = MiniMessage.miniMessage()
            .deserialize("<red>Uh oh... <gray>prepare for <italic>lots</italic> of explosions; <yellow>the TNT rain event just started");

    public TNTRainEvent(@NotNull BlockSumoGame game) {
        super(game, START_MESSAGE);
    }

    @Override
    public void start() {
        final Instance instance = game.getInstance();
        instance.scheduler().submitTask(new Supplier<>() {
            int i = 0;

            @Override
            public TaskSchedule get() {
                if (i >= 4) return TaskSchedule.stop();

                for (final Player player : game.getPlayers()) {
                    // TODO: Uncomment when we don't put everyone in creative for testing
//                    if (player.getGameMode() != GameMode.SURVIVAL) continue;
                    spawnTntOnPlayer(instance, player);
                }

                i++;
                return TaskSchedule.seconds(2);
            }
        });
    }

    private void spawnTntOnPlayer(@NotNull Instance instance, @NotNull Player player) {
        final Pos pos = player.getPosition();
        final Pos tntPos = new Pos(pos.x(), MapData.CENTER.y() + 10, pos.z());

        Entity tntEntity = new Entity(EntityType.TNT);
        PrimedTntMeta meta = (PrimedTntMeta) tntEntity.getEntityMeta();
        meta.setFuseTime(80);

        tntEntity.setInstance(instance, tntPos);
        this.game.getAudience().playSound(Sound.sound(SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, 2, 1), tntEntity);
    }
}
