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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PlayerTeamManager {

    private final List<Integer> teamColours = new ArrayList<>();

    public void allocateTeam(@NotNull Player player) {
        int randomColour = -1;
        while (randomColour == -1 || teamColours.contains(randomColour)) { // Stop collisions
            randomColour = Color.HSBtoRGB(ThreadLocalRandom.current().nextFloat(), 1f, 1f);
        }

        teamColours.add(randomColour);

        player.setTag(PlayerTags.TEAM_COLOR, new TeamColor(TextColor.color(randomColour)));
    }

    public void setTeam(@NotNull Player player) {
        TeamColor allocatedColor = player.getTag(PlayerTags.TEAM_COLOR);

        Team minestomTeam = MinecraftServer.getTeamManager().createBuilder(allocatedColor.getTextColor().asHexString())
                .teamColor(allocatedColor.getNamedTextColor())
                .collisionRule(TeamsPacket.CollisionRule.NEVER)
                .updateTeamPacket()
                .build();

        player.setTeam(minestomTeam);

        this.updateTeamLives(minestomTeam, 5, allocatedColor);
    }

    public void updateTeamLives(@NotNull Player player, int lives) {
        TeamColor allocatedColor = player.getTag(PlayerTags.TEAM_COLOR);
        updateTeamLives(player.getTeam(), lives, allocatedColor);
    }

    public void updateTeamLives(@NotNull Team team, int lives, TeamColor color) {
        TextColor livesColor;
        if (lives == 5) {
            livesColor = NamedTextColor.GREEN;
        } else {
            livesColor = TextColor.lerp((lives - 1) / 4F, NamedTextColor.RED, NamedTextColor.GREEN);
        }
        if (lives > 5) {
            livesColor = NamedTextColor.LIGHT_PURPLE;
        }

        team.setSuffix(Component.text()
                .append(Component.text(" • ", NamedTextColor.GRAY))
                .append(Component.text(lives, livesColor, TextDecoration.BOLD))
                .build());

        for (Player player : team.getPlayers()) {
            player.setDisplayName(
                    Component.text()
                            .append(Component.text(player.getUsername(), color.getTextColor()))
                            .append(team.getSuffix())
                            .build()
            );
        }
    }
}
