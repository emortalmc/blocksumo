package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.BlockSumoModule;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class PlayerDisconnectHandler {

    private final BlockSumoGame game;
    private final PlayerManager playerManager;

    public PlayerDisconnectHandler(@NotNull BlockSumoGame game, @NotNull PlayerManager playerManager) {
        this.game = game;
        this.playerManager = playerManager;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerDisconnectEvent.class, event -> onDisconnect(event.getPlayer()));
    }

    private void onDisconnect(@NotNull Player player) {
        playerManager.cleanUpPlayer(player);
        removePlayer(player);

        sendQuitMessage(player);
        playQuitSound();

        cancelCountdownIfNeeded();
        final boolean singlePlayerWinner = hasSinglePlayerWinner();
        if (singlePlayerWinner) endWithSinglePlayerWinner();

        playerManager.getRespawnHandler().cleanUpPlayer(player);
        game.getSpawnProtectionManager().cleanUpPlayer(player);

        playerManager.cleanUpPlayer(player);
        playerManager.removeFromScoreboard(player);

        if (!singlePlayerWinner) checkForWinner();
    }

    private void removePlayer(@NotNull Player left) {
        game.getPlayers().remove(left);
    }

    private void sendQuitMessage(@NotNull Player left) {
        final Component message = Component.text()
                .append(Component.text("QUIT", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(left.getUsername(), NamedTextColor.RED))
                .append(Component.text(" left the game", NamedTextColor.GRAY))
                .build();
        game.getAudience().sendMessage(message);
    }

    private void playQuitSound() {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.MASTER, 1F, 0.5F);
        game.getAudience().playSound(sound);
    }

    private void cancelCountdownIfNeeded() {
        if (game.getPlayers().size() >= BlockSumoModule.MIN_PLAYERS) return;
        game.cancelCountdown();
    }

    private boolean hasSinglePlayerWinner() {
        return game.getPlayers().size() == 1;
    }

    private void endWithSinglePlayerWinner() {
        final Player winner = game.getPlayers().iterator().next();
        game.victory(Set.of(winner));
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
}
