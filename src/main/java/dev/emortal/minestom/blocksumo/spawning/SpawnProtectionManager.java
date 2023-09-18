package dev.emortal.minestom.blocksumo.spawning;

import dev.emortal.minestom.blocksumo.game.PlayerTags;
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
        return this.getProtectionTime(player) != 0;
    }

    private long getProtectionTime(@NotNull Player player) {
        long now = System.currentTimeMillis();
        long start = player.getTag(PlayerTags.SPAWN_PROTECTION_TIME);

        if (start == 0 || now > start) return 0;
        return start - now;
    }

    private void setProtectionTime(@NotNull Player player, long time) {
        long protectionTime = time == 0 ? 0L : System.currentTimeMillis() + time;
        player.setTag(PlayerTags.SPAWN_PROTECTION_TIME, protectionTime);
    }

    public void startProtection(@NotNull Player player, long time) {
        this.setProtectionTime(player, time);
        Task task = player.scheduler()
                .buildTask(() -> this.playProtectionEndSound(player))
                .delay(TaskSchedule.millis(time))
                .schedule();
        this.protectionIndicatorTasks.put(player.getUuid(), task);
    }

    public void endProtection(@NotNull Player player) {
        this.playProtectionEndSound(player);
        this.setProtectionTime(player, 0);

        Task task = this.protectionIndicatorTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }

    private void playProtectionEndSound(@NotNull Player player) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_GENERIC_EXTINGUISH_FIRE, Sound.Source.MASTER, 0.75F, 1F);
        player.playSound(sound, Sound.Emitter.self());
    }

    public void notifyProtected(@NotNull Player attacker, @NotNull Player victim) {
        this.playProtectedSound(attacker, victim.getPosition());
        this.playProtectedSound(victim, attacker.getPosition());
    }

    private void playProtectedSound(@NotNull Player player, @NotNull Pos source) {
        Sound sound = Sound.sound(SoundEvent.BLOCK_WOOD_BREAK, Sound.Source.MASTER, 0.75F, 1.5F);
        player.playSound(sound, source.x(), source.y(), source.z());
    }

    public void cleanUpPlayer(@NotNull Player player) {
        Task task = this.protectionIndicatorTasks.remove(player.getUuid());
        if (task != null) task.cancel();
    }
}
