package dev.emortal.minestom.blocksumo.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class SumoTeam implements PacketGroupingAudience {

    private final String teamName;
    private final TeamColor color;

    private final Set<Player> players = new CopyOnWriteArraySet<>();
    private final Team scoreboardTeam;

    public SumoTeam(@NotNull String teamName, @NotNull TeamColor color) {
        this.teamName = teamName;
        this.color = color;
        this.scoreboardTeam = createScoreboardTeam();
    }

    public @NotNull Team getScoreboardTeam() {
        return scoreboardTeam;
    }

    private Team createScoreboardTeam() {
        return MinecraftServer.getTeamManager().createBuilder(teamName)
                .teamColor(NamedTextColor.nearestTo(TextColor.color(color.getColor())))
                .collisionRule(TeamsPacket.CollisionRule.NEVER)
                .updateTeamPacket()
                .build();
    }

    public void setSuffix(@NotNull Component suffix) {
        scoreboardTeam.updateSuffix(suffix);
    }

    public void addPlayer(@NotNull Player player) {
        player.setTeam(scoreboardTeam);
        players.add(player);
    }

    public void removePlayer(@NotNull Player player) {
        player.setTeam(null);
        players.remove(player);
    }

    public void destroy() {
        for (final Player player : players) {
            player.setTeam(null);
        }
        players.clear();
        MinecraftServer.getTeamManager().deleteTeam(scoreboardTeam);
    }

    @Override
    public @NotNull Set<Player> getPlayers() {
        return players;
    }
}
