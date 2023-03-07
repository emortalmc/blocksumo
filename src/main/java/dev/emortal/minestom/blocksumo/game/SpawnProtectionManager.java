package dev.emortal.minestom.blocksumo.game;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpawnProtectionManager {

    private final Map<UUID, Task> protectionIndicatorTasks = new ConcurrentHashMap<>();

    public boolean isProtected(@NotNull Player player) {
        return getProtectionTime(player) != 0;
    }

    private long getProtectionTime(@NotNull Player player) {
        final long time = player.getTag(PlayerTags.SPAWN_PROTECTION_TIME);
        if (time == 0 || System.currentTimeMillis() > time) return 0;
        return time - System.currentTimeMillis();
    }

    private void setProtectionTime(@NotNull Player player, long time) {
        final long protectionTime = time == 0 ? 0L : System.currentTimeMillis() + time;
        player.setTag(PlayerTags.SPAWN_PROTECTION_TIME, protectionTime);
    }

    public void startProtection(@NotNull Player player, long time) {
        setProtectionTime(player, time);
        final Task task = player.scheduler().buildTask(() -> playProtectionEndSound(player))
                .delay(TaskSchedule.millis(time))
                .schedule();
        protectionIndicatorTasks.put(player.getUuid(), task);
    }

    public void endProtection(@NotNull Player player) {
        playProtectionEndSound(player);
        setProtectionTime(player, 0);

        final Task task = protectionIndicatorTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }

    private void playProtectionEndSound(@NotNull Player player) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_GENERIC_EXTINGUISH_FIRE, Sound.Source.MASTER, 0.75F, 1F);
        player.playSound(sound, Sound.Emitter.self());
    }

    public void notifyProtected(@NotNull Player attacker, @NotNull Player victim) {
        playProtectedSound(attacker, victim.getPosition());
        playProtectedSound(victim, attacker.getPosition());
    }

    private void playProtectedSound(@NotNull Player player, @NotNull Pos source) {
        final Sound sound = Sound.sound(SoundEvent.BLOCK_WOOD_BREAK, Sound.Source.MASTER, 0.75F, 1.5F);
        player.playSound(sound, source.x(), source.y(), source.z());
    }

    public void cleanUpPlayer(@NotNull Player player) {
        final Task task = protectionIndicatorTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }
}
