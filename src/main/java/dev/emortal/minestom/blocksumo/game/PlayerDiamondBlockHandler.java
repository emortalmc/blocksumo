package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.damage.PlayerDeathHandler;
import dev.emortal.minestom.blocksumo.map.MapData;
import dev.emortal.minestom.blocksumo.team.TeamColor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

public final class PlayerDiamondBlockHandler {
    private static final Point SPAWN = MapData.CENTER.sub(0.5, 0, 0.5);
    private static final int DIAMOND_BLOCK_SECONDS = 20;

    private final @NotNull BlockSumoGame game;

    private @Nullable Player playerOnDiamondBlock = null;
    private @Nullable Task diamondBlockTask = null;

    public PlayerDiamondBlockHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerMoveEvent.class, this::onPlayerMove);
    }

    private boolean isValidMove(@NotNull PlayerMoveEvent event) {
        if (this.game.hasEnded()) return false;
        if (event.getPlayer().getTeam() == PlayerDeathHandler.DEAD_TEAM) return false;
        return event.getNewPosition().sameBlock(SPAWN);
    }

    private void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isValidMove(event)) {
            this.stopStandingOnBlock(player);
            return;
        }

        if (this.playerOnDiamondBlock != null) return;
        // player is now standing on the diamond block
        this.playerOnDiamondBlock = player;

        this.diamondBlockTask = player.scheduler().submitTask(new DiamondBlockTask(player));
    }

    private void stopStandingOnBlock(@NotNull Player player) {
        if (this.playerOnDiamondBlock != player) return;

        this.stop();
    }

    private void stop() {
        if (this.playerOnDiamondBlock != null) {
            this.playerOnDiamondBlock.setLevel(0);
        }
        this.playerOnDiamondBlock = null;

        if (this.diamondBlockTask != null) {
            this.diamondBlockTask.cancel();
        }
    }

    private final class DiamondBlockTask implements Supplier<TaskSchedule> {

        private final @NotNull Player player;
        private final @NotNull TeamColor teamColor;
        private final @NotNull BlockSumoGame game = PlayerDiamondBlockHandler.this.game;

        private int secondsLeft = DIAMOND_BLOCK_SECONDS;

        DiamondBlockTask(@NotNull Player player) {
            this.player = player;
            this.teamColor = player.getTag(PlayerTags.TEAM_COLOR);
        }

        @Override
        public TaskSchedule get() {
            if (this.game.hasEnded()) {
                PlayerDiamondBlockHandler.this.stop();
                return TaskSchedule.stop();
            }

            if (this.secondsLeft == 0) {
                this.game.victory(Set.of(this.player));
                return TaskSchedule.stop();
            }

            this.player.setLevel(this.secondsLeft);
            this.player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1f, 1.5f), Sound.Emitter.self());

            if (this.secondsLeft <= 5) {
                this.notifyFinalCountdown();
            }
            if (this.secondsLeft % 5 == 0 && this.secondsLeft != DIAMOND_BLOCK_SECONDS) {
                this.notifyCountdown();
            }

            this.secondsLeft--;
            return TaskSchedule.seconds(1);
        }

        private void notifyCountdown() {
            this.game.sendMessage(Component.text()
                    .append(Component.text("!", NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(this.player.getUsername(), TextColor.color(this.teamColor.getColor()), TextDecoration.BOLD))
                    .append(Component.text(" is standing on the diamond block!\n", NamedTextColor.GRAY))
                    .append(Component.text("!", NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("They win in ", NamedTextColor.GRAY))
                    .append(Component.text(this.secondsLeft, NamedTextColor.RED))
                    .append(Component.text(" seconds!", NamedTextColor.GRAY))
                    .build());

            this.game.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.MASTER, 1f, 1.2f), Sound.Emitter.self());
        }

        private void notifyFinalCountdown() {
            TextColor titleColor = TextColor.lerp(this.secondsLeft / 5f, NamedTextColor.RED, NamedTextColor.GREEN);
            this.game.showTitle(Title.title(
                    Component.text(this.secondsLeft, titleColor, TextDecoration.BOLD),
                    Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)
            ));

            this.game.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 0.8f), Sound.Emitter.self());
        }
    }
}
