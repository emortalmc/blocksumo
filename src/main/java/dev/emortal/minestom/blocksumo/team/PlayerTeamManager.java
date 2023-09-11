package dev.emortal.minestom.blocksumo.team;

import dev.emortal.minestom.blocksumo.game.PlayerTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class PlayerTeamManager {
    private static final List<TeamColor> COLORS = List.of(TeamColor.values());
    private static final Random RANDOM = new Random();

    private final List<TeamColor> remainingColors = new ArrayList<>(COLORS);
    private final Map<TeamColor, SumoTeam> teams = new EnumMap<>(TeamColor.class);

    public void allocateTeam(@NotNull Player player) {
        TeamColor allocatedColor = this.allocateTeamColor();

        SumoTeam team = new SumoTeam(allocatedColor.toString().toLowerCase(Locale.ROOT), allocatedColor);
        this.teams.put(allocatedColor, team);

        this.updateTeamLives(team, allocatedColor, player, 5);
        player.setTag(PlayerTags.TEAM_COLOR, allocatedColor);
    }

    public void resetTeam(@NotNull Player player) {
        Team team = MinecraftServer.getTeamManager().createBuilder(player.getUsername() + "_default")
                .collisionRule(TeamsPacket.CollisionRule.NEVER)
                .build();
        player.setTeam(team);
    }

    public void updateTeamLives(@NotNull Player player, int lives) {
        TeamColor color = player.getTag(PlayerTags.TEAM_COLOR);

        SumoTeam team = this.teams.get(color);
        if (team == null) {
            throw new IllegalStateException("Team for player " + player.getUsername() + " is null!");
        }

        this.updateTeamLives(team, color, player, lives);
    }

    private void updateTeamLives(@NotNull SumoTeam team, @NotNull TeamColor teamColor, @NotNull Player player, int lives) {
        TextColor livesColor;
        if (lives == 5) {
            livesColor = NamedTextColor.GREEN;
        } else {
            livesColor = TextColor.lerp((lives - 1) / 4F, NamedTextColor.RED, NamedTextColor.GREEN);
        }

        team.setSuffix(Component.text()
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(lives, livesColor, TextDecoration.BOLD))
                .build());

        Component displayName = Component.text()
                .append(Component.text(player.getUsername(), TextColor.color(teamColor.getColor())))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(lives, livesColor, TextDecoration.BOLD))
                .build();
        player.setDisplayName(displayName);
    }

    private @NotNull TeamColor allocateTeamColor() {
        // We will try to allocate all the colours before we start reusing them.
        if (this.remainingColors.isEmpty()) return getRandomColor();

        int randomIndex = RANDOM.nextInt(this.remainingColors.size());
        TeamColor selectedColor = this.remainingColors.get(randomIndex);

        this.remainingColors.remove(randomIndex);
        return selectedColor;
    }

    private @NotNull TeamColor getRandomColor() {
        int randomIndex = RANDOM.nextInt(COLORS.size());
        return COLORS.get(randomIndex);
    }

    public void removeAllTeams() {
        for (SumoTeam team : this.teams.values()) {
            MinecraftServer.getTeamManager().deleteTeam(team.getScoreboardTeam());
        }
    }
}
