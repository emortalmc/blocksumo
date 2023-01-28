package dev.emortal.minestom.blocksumo.damage;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerManager;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class PlayerDeathHandler {

    private final BlockSumoGame game;
    private final PlayerManager playerManager;
    private final int minAllowedHeight;

    public PlayerDeathHandler(@NotNull BlockSumoGame game, @NotNull PlayerManager playerManager, int minAllowedHeight) {
        this.game = game;
        this.playerManager = playerManager;
        this.minAllowedHeight = minAllowedHeight;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
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
        if (isValidDamageTimestamp(player) && lastDamageSource != null) {
            if (lastDamageSource instanceof EntityDamage damage) {
                killer = getKillerFromDamage(damage);
            } else if (lastDamageSource instanceof EntityProjectileDamage damage) {
                killer = damage.getShooter();
            }
        }
        return killer != player ? killer : null;
    }

    private boolean isValidDamageTimestamp(@NotNull Player player) {
        // Last damage time is only valid for 8 seconds after the damage.
        return getLastDamageTime(player) + 8000 > System.currentTimeMillis();
    }

    private @Nullable Entity getKillerFromDamage(@NotNull EntityDamage damage) {
        final Entity source = damage.getSource();
        if (source instanceof Player player) return player;
        // TODO: Check if the source is a power up.
        return null;
    }

    private long getLastDamageTime(@NotNull Player player) {
        return player.getTag(PlayerTags.LAST_DAMAGE_TIME);
    }

    public boolean isDead(@NotNull Player player) {
        return player.getTag(PlayerTags.DEAD);
    }

    public void kill(@NotNull Player player, @Nullable Entity killer) {
        player.setTag(PlayerTags.DEAD, true);

        makeSpectator(player);
        playDeathSound(player);

        player.setCanPickupItem(false);
        player.getInventory().clear();
        player.setVelocity(new Vec(0, 40, 0));
        final int remainingLives = player.getTag(PlayerTags.LIVES) - 1;
        player.setTag(PlayerTags.LIVES, (byte) remainingLives);

        sendKillMessage(player, killer, remainingLives);
        sendVictimTitle(player, killer, remainingLives);

        if (remainingLives <= 0) {
            playerManager.getScoreboard().removeLine(player.getUuid().toString());
            player.setTeam(null);
            checkForWinner();
            playerManager.getTeamManager().resetTeam(player);
            return;
        }

        updateScoreboardLives(player, remainingLives);
        playerManager.getRespawnHandler().scheduleRespawn(player, () -> player.setTag(PlayerTags.DEAD, false));
    }

    private void checkForWinner() {
        final Set<Player> alivePlayers = new HashSet<>();
        for (final Player player : game.getPlayers()) {
            if (player.getTag(PlayerTags.LIVES) > 0) alivePlayers.add(player);
        }
        if (alivePlayers.isEmpty()) return;

        final Player firstPlayer = alivePlayers.iterator().next();
        for (final Player alive : alivePlayers) {
            if (alive.getTag(PlayerTags.TEAM_COLOR) != firstPlayer.getTag(PlayerTags.TEAM_COLOR)) return;
        }
        game.victory(alivePlayers);
    }

    private void updateScoreboardLives(@NotNull Player player, int remainingLives) {
        playerManager.getTeamManager().updateTeamLives(player, remainingLives);

        final Sidebar scoreboard = playerManager.getScoreboard();
        final String lineName = player.getUuid().toString();
        scoreboard.updateLineContent(lineName, player.getDisplayName());
        scoreboard.updateLineScore(lineName, remainingLives);
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

    private void sendKillMessage(@NotNull Player victim, @Nullable Entity killer, int remainingLives) {
        final TextComponent.Builder message = Component.text()
                .append(Component.text("☠", NamedTextColor.RED))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(victim.getUsername(), NamedTextColor.WHITE));

        if (killer instanceof Player playerKiller) {
            message.append(Component.text(" was killed by ", NamedTextColor.GRAY));
            message.append(Component.text(playerKiller.getUsername(), NamedTextColor.WHITE));
        } else {
            message.append(Component.text(" died", NamedTextColor.GRAY));
        }

        if (remainingLives <= 0) {
            message.append(Component.text(" FINAL KILL", NamedTextColor.AQUA, TextDecoration.BOLD));
        }

        playerManager.broadcastMessage(message.build());
    }

    private void sendVictimTitle(@NotNull Player victim, @Nullable Entity killer, int remainingLives) {
        final Component subtitle;
        if (killer instanceof Player playerKiller) {
            subtitle = Component.text()
                    .append(Component.text("Killed by ", NamedTextColor.GRAY))
                    .append(Component.text(playerKiller.getUsername(), NamedTextColor.WHITE))
                    .build();
        } else if (remainingLives <= 0) {
            subtitle = Component.text("(Final kill)", NamedTextColor.DARK_GRAY);
        } else {
            subtitle = Component.empty();
        }

        final Duration stay = Duration.ofSeconds(remainingLives <= 0 ? 2 : 1);
        final Title title = Title.title(
                Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
                subtitle,
                Title.Times.times(Duration.ZERO, stay, Duration.ofSeconds(1))
        );
        victim.showTitle(title);
    }
}
