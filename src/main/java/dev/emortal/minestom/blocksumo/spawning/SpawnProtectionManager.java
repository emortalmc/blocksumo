package dev.emortal.minestom.blocksumo.spawning;

import dev.emortal.minestom.blocksumo.game.PlayerTags;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class SpawnProtectionManager {

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
    }

    public void endProtection(@NotNull Player player) {
        this.setProtectionTime(player, 0);
    }

    public void notifyProtected(@NotNull Player attacker, @NotNull Player victim) {
        this.playProtectedSound(attacker, victim.getPosition());
        this.playProtectedSound(victim, attacker.getPosition());
    }

    private void playProtectedSound(@NotNull Player player, @NotNull Pos source) {
        Sound sound = Sound.sound(SoundEvent.BLOCK_WOOD_BREAK, Sound.Source.MASTER, 0.75F, 1.5F);
        player.playSound(sound, source.x(), source.y(), source.z());
    }
}
