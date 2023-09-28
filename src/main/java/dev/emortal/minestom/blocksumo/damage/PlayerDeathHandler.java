package dev.emortal.minestom.blocksumo.damage;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerManager;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.spawning.PlayerRespawnHandler;
import dev.emortal.minestom.blocksumo.team.TeamColor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
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
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class PlayerDeathHandler {
    private static final Team DEAD_TEAM = MinecraftServer.getTeamManager().createBuilder("dead")
            .teamColor(NamedTextColor.GRAY)
            .prefix(Component.text("☠ ", NamedTextColor.GRAY))
            .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER)
            .updateTeamPacket()
            .build();

    private final @NotNull BlockSumoGame game;
    private final @NotNull PlayerManager playerManager;
    private final @NotNull PlayerRespawnHandler respawnHandler;
    private final int minAllowedHeight;

    public PlayerDeathHandler(@NotNull BlockSumoGame game, @NotNull PlayerManager playerManager, @NotNull PlayerRespawnHandler respawnHandler,
                              int minAllowedHeight) {
        this.game = game;
        this.playerManager = playerManager;
        this.respawnHandler = respawnHandler;
        this.minAllowedHeight = minAllowedHeight;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerTickEvent.class, this::onTick);
    }

    private void onTick(@NotNull PlayerTickEvent event) {
        Player player = event.getPlayer();
        if (this.isDead(player)) return;

        Entity killer = this.determineKiller(player);
        if (this.isUnderMinAllowedHeight(player)) {
            this.kill(player, killer);
        }
    }

    private boolean isUnderMinAllowedHeight(@NotNull Player player) {
        return player.getPosition().y() < this.minAllowedHeight;
    }

    private @Nullable Entity determineKiller(@NotNull Player player) {
        Entity killer = null;
        DamageType lastDamageSource = player.getLastDamageSource();

        if (this.isValidDamageTimestamp(player) && lastDamageSource != null) {
            if (lastDamageSource instanceof EntityDamage damage) {
                killer = this.getKillerFromDamage(damage);
            } else if (lastDamageSource instanceof EntityProjectileDamage damage) {
                killer = damage.getShooter();
            }
        }

        return killer != player ? killer : null;
    }

    private boolean isValidDamageTimestamp(@NotNull Player player) {
        // Last damage time is only valid for 8 seconds after the damage.
        return this.getLastDamageTime(player) + 8000 > System.currentTimeMillis();
    }

    private @Nullable Entity getKillerFromDamage(@NotNull EntityDamage damage) {
        Entity source = damage.getSource();
        if (source instanceof Player player) return player;
        // TODO: Check if the source is a power up.
        return null;
    }

    private long getLastDamageTime(@NotNull Player player) {
        return player.getTag(PlayerTags.LAST_DAMAGE_TIME);
    }

    public boolean isDead(@NotNull Player player) {
        return player.getTeam() == DEAD_TEAM;
    }

    public void kill(@NotNull Player player, @Nullable Entity killer) {
        Team beforeTeam = player.getTeam();
        player.setTeam(DEAD_TEAM);

        this.makeSpectator(player);
        this.playDeathSound(player);

        player.setCanPickupItem(false);
        player.getInventory().clear();
        player.setVelocity(new Vec(0, 40, 0));

        int remainingLives = player.getTag(PlayerTags.LIVES) - 1;
        player.setTag(PlayerTags.LIVES, (byte) remainingLives);

        this.sendKillMessage(player, killer, remainingLives);
        this.sendVictimTitle(player, killer, remainingLives);

        if (remainingLives <= 0) {
            this.playerManager.removeDeadPlayer(player);
            this.checkForWinner();
            return;
        }

        this.playerManager.updateRemainingLives(player, remainingLives);
        this.respawnHandler.scheduleRespawn(player, () -> player.setTeam(beforeTeam));
    }

    private void checkForWinner() {
        Set<Player> alivePlayers = new HashSet<>();
        for (Player player : this.game.getPlayers()) {
            if (player.getTag(PlayerTags.LIVES) > 0) alivePlayers.add(player);
        }
        if (alivePlayers.isEmpty()) return;

        Player firstPlayer = alivePlayers.iterator().next();
        for (Player alive : alivePlayers) {
            if (alive.getTag(PlayerTags.TEAM_COLOR) != firstPlayer.getTag(PlayerTags.TEAM_COLOR)) return;
        }
        this.game.victory(alivePlayers);
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
        TeamColor victimTeam = victim.getTag(PlayerTags.TEAM_COLOR);

        TextComponent.Builder message = Component.text()
                .append(Component.text("☠", NamedTextColor.RED))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(victim.getUsername(), TextColor.color(victimTeam.getColor())));

        if (killer instanceof Player playerKiller) {
            TeamColor killerTeam = playerKiller.getTag(PlayerTags.TEAM_COLOR);

            message.append(Component.text(" was killed by ", NamedTextColor.GRAY));
            message.append(Component.text(playerKiller.getUsername(), TextColor.color(killerTeam.getColor())));
        } else {
            message.append(Component.text(" died", NamedTextColor.GRAY));
        }

        if (remainingLives <= 0) {
            message.append(Component.text(" FINAL KILL", NamedTextColor.AQUA, TextDecoration.BOLD));
        }

        this.game.sendMessage(message.build());
    }

    private void sendVictimTitle(@NotNull Player victim, @Nullable Entity killer, int remainingLives) {
        Component subtitle;
        if (killer instanceof Player playerKiller) {
            TeamColor killerTeam = playerKiller.getTag(PlayerTags.TEAM_COLOR);

            subtitle = Component.text()
                    .append(Component.text("Killed by ", NamedTextColor.GRAY))
                    .append(Component.text(playerKiller.getUsername(), TextColor.color(killerTeam.getColor())))
                    .build();
        } else if (remainingLives <= 0) {
            subtitle = Component.text("(Final kill)", NamedTextColor.DARK_GRAY);
        } else {
            subtitle = Component.empty();
        }

        Duration stay = Duration.ofSeconds(remainingLives <= 0 ? 2 : 1);
        Title title = Title.title(
                Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
                subtitle,
                Title.Times.times(Duration.ZERO, stay, Duration.ofSeconds(1))
        );
        victim.showTitle(title);
    }
}
