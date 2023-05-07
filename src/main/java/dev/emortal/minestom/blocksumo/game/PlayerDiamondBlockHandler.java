package dev.emortal.minestom.blocksumo.game;

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

    private final BlockSumoGame game;
    private @Nullable Player playerOnDiamondBlock = null;
    private @Nullable Task diamondBlockTask = null;

    public PlayerDiamondBlockHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerMoveEvent.class, this::onPlayerMove);
    }

    private void onPlayerMove(@NotNull PlayerMoveEvent event) {
        if (event.getNewPosition().sameBlock(SPAWN)) {
            // player is now standing on the diamond block
            if (playerOnDiamondBlock == null) {
                playerOnDiamondBlock = event.getPlayer();

                TeamColor playerTeam = event.getPlayer().getTag(PlayerTags.TEAM_COLOR);

                diamondBlockTask = event.getPlayer().scheduler().submitTask(new Supplier<>() {
                    int secondsLeft = DIAMOND_BLOCK_SECONDS;

                    @Override
                    public TaskSchedule get() {
                        if (secondsLeft == 0) {
                            game.victory(Set.of(event.getPlayer()));

                            return TaskSchedule.stop();
                        }

                        event.getPlayer().setLevel(secondsLeft);
                        event.getPlayer().playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1f, 1.5f), Sound.Emitter.self());

                        if (secondsLeft <= 5) {
                            game.getAudience().showTitle(Title.title(
                                    Component.text(secondsLeft, TextColor.lerp(secondsLeft / 5f, NamedTextColor.RED, NamedTextColor.GREEN), TextDecoration.BOLD),
                                    Component.empty(),
                                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)
                            ));

                            game.getAudience().playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 0.8f), Sound.Emitter.self());
                        }
                        if (secondsLeft % 5 == 0 && secondsLeft != DIAMOND_BLOCK_SECONDS) {
                            game.getAudience().sendMessage(
                                    Component.text()
                                            .append(Component.text("!", NamedTextColor.RED, TextDecoration.BOLD))
                                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                                            .append(Component.text(event.getPlayer().getUsername(), TextColor.color(playerTeam.getColor()), TextDecoration.BOLD))
                                            .append(Component.text(" is standing on the diamond block!\n", NamedTextColor.GRAY))
                                            .append(Component.text("!", NamedTextColor.RED, TextDecoration.BOLD))
                                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                                            .append(Component.text("They win in ", NamedTextColor.GRAY))
                                            .append(Component.text(secondsLeft, NamedTextColor.RED))
                                            .append(Component.text(" seconds!", NamedTextColor.GRAY))
                                            .build()
                            );
                            game.getAudience().playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.MASTER, 1f, 1.2f), Sound.Emitter.self());
                        }

                        secondsLeft--;

                        return TaskSchedule.seconds(1);
                    }
                });
            }
        } else {
            if (playerOnDiamondBlock == event.getPlayer()) {
                // player is no longer standing on the diamond block
                playerOnDiamondBlock.setLevel(0);
                playerOnDiamondBlock = null;
                if (diamondBlockTask != null) diamondBlockTask.cancel();


            }
        }
    }
}
