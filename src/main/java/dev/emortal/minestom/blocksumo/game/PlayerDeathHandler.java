package dev.emortal.minestom.blocksumo.game;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.damage.EntityProjectileDamage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerDeathHandler {

    private final PlayerTracker playerTracker;
    private final Set<UUID> deadPlayers = new HashSet<>();
    private final int minAllowedHeight;

    public PlayerDeathHandler(final PlayerTracker playerTracker, final int minAllowedHeight) {
        this.playerTracker = playerTracker;
        this.minAllowedHeight = minAllowedHeight;
    }

    public void registerDeathListener(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerTickEvent.class, event -> {
            final Player player = event.getPlayer();
            if (isDead(player)) return;

            final Entity killer = determineKiller(player);
            if (isUnderMinAllowedHeight(player)) kill(player, killer);
        });
    }

    private boolean isUnderMinAllowedHeight(@NotNull Player player) {
        return player.getPosition().y() < minAllowedHeight;
    }

    private @Nullable Entity determineKiller(@NotNull Player player) {
        Entity killer = null;
        final DamageType lastDamageSource = player.getLastDamageSource();
        if (getLastDamageTime(player) + 8000 > System.currentTimeMillis() && lastDamageSource != null) {
            if (lastDamageSource instanceof EntityDamage damage) {
                killer = getKillerFromDamage(damage);
            } else if (lastDamageSource instanceof EntityProjectileDamage damage) {
                killer = damage.getShooter();
            }
        }
        return killer != player ? killer : null;
    }

    private @Nullable Entity getKillerFromDamage(@NotNull EntityDamage damage) {
        final Entity source = damage.getSource();
        if (source instanceof Player player) return player;
        // TODO: Check if the source is a power up.
        return null;
    }

    private static long getLastDamageTime(@NotNull Player player) {
        return player.getTag(PlayerTags.LAST_DAMAGE_TIME);
    }

    public boolean isDead(@NotNull Player player) {
        return deadPlayers.contains(player.getUuid());
    }

    public void kill(@NotNull Player player, @Nullable Entity killer) {
        deadPlayers.add(player.getUuid());

        makeSpectator(player);
        playDeathSound(player);

        player.setCanPickupItem(false);
        player.getInventory().clear();
        player.setVelocity(new Vec(0, 40, 0));

        sendVictimTitle(player, killer);

        playerTracker.getRespawnHandler().scheduleRespawn(player, () -> deadPlayers.remove(player.getUuid()));
    }

    private void makeSpectator(final @NotNull Player player) {
        player.clearEffects();
        player.heal();
        player.setInvisible(true);
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void playDeathSound(final @NotNull Player player) {
        player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_DEATH, Sound.Source.PLAYER, 1, 1), Sound.Emitter.self());
    }

    private void sendVictimTitle(@NotNull Player victim, @Nullable Entity killer) {
        final Component subtitle;
        if (killer instanceof Player playerKiller) {
            subtitle = Component.text()
                    .append(Component.text("Killed by ", NamedTextColor.GRAY))
                    .append(Component.text(playerKiller.getUsername(), NamedTextColor.WHITE))
                    .build();

        } else {
            subtitle = Component.empty();
        }

        final Title title = Title.title(
                Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
                subtitle,
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(1))
        );
        victim.showTitle(title);
    }
}
