package dev.emortal.minestom.blocksumo.scoreboard;

import com.google.common.collect.Sets;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.utils.text.TextUtil;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScoreboardManager implements Viewable {

    private static final @NotNull Component FOOTER = Component.text()
            .append(Component.text(TextUtil.convertToSmallFont("mc.emortal.dev"), NamedTextColor.DARK_GRAY))
            .append(Component.text("       ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .build();

    // max lines is 12 because we take up 3 of our 15 lines with the header and footer
    private static final int MAX_LINES = 12;

    private final @NotNull Scoreboard scoreboard = new Scoreboard(BlockSumoGame.TITLE);

    private @Nullable Set<Player> scores;

    public ScoreboardManager() {
        this.scoreboard.createLine(new Scoreboard.ScoreboardLine("header_spacer", Component.empty(), 99));
        this.scoreboard.createLine(new Scoreboard.ScoreboardLine("footer_spacer", Component.empty(), -8));
        this.scoreboard.createLine(new Scoreboard.ScoreboardLine("footer", FOOTER, -9));
    }

    public void updateScoreboard(@NotNull Set<Player> players) {
        Set<Player> newScores = players.stream()
                .sorted(Comparator.<Player, Byte>comparing(player -> player.getTag(PlayerTags.LIVES)).reversed())
                .limit(MAX_LINES)
                .collect(Collectors.toSet());

        if (this.scores == null) {
            // this is the first update, so we just create all the lines
            this.scores = newScores;
            for (Player player : newScores) this.scoreboard.createLine(createLine(player));
            return;
        }

        // remove any players that are no longer within the top MAX_LINES
        Sets.SetView<Player> removed = Sets.difference(this.scores, newScores);
        for (Player player : removed) this.scoreboard.removeLine(player.getUuid().toString());

        // add any players that are now within the top MAX_LINES
        Sets.SetView<Player> added = Sets.difference(newScores, this.scores);
        for (Player player : added) this.scoreboard.createLine(createLine(player));

        // update the content of any players that are still within the top MAX_LINES
        Sets.SetView<Player> updated = Sets.intersection(newScores, this.scores);
        for (Player player : updated) {
            byte lives = player.getTag(PlayerTags.LIVES);
            this.scoreboard.updateLineContent(player.getUuid().toString(), createScoreboardComponent(player, lives));
            this.scoreboard.updateLineScore(player.getUuid().toString(), lives);
        }

        this.scores = newScores;
    }

    private @NotNull Scoreboard.ScoreboardLine createLine(@NotNull Player player) {
        byte lives = player.getTag(PlayerTags.LIVES);
        return new Scoreboard.ScoreboardLine(player.getUuid().toString(), createScoreboardComponent(player, lives), lives);
    }

    private @NotNull Component createScoreboardComponent(@NotNull Player player, byte lives) {
        TextColor livesColor;
        if (lives == 5) {
            livesColor = NamedTextColor.GREEN;
        } else {
            livesColor = TextColor.lerp((lives - 1) / 4F, NamedTextColor.RED, NamedTextColor.GREEN);
        }

        Team team = player.getTeam();
        if (team == null) throw new IllegalStateException("Player " + player.getUsername() + " has no team!");

        return Component.text()
                .append(Component.text(player.getUsername(), team.getTeamColor()))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(lives, livesColor, TextDecoration.BOLD))
                .build();
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        return this.scoreboard.addViewer(player);
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        return this.scoreboard.removeViewer(player);
    }

    @Override
    public @NotNull Set<@NotNull Player> getViewers() {
        return this.scoreboard.getViewers();
    }

}
