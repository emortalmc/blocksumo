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

    private final @NotNull String teamName;
    private final @NotNull TeamColor color;
    private final @NotNull Team scoreboardTeam;

    private final Set<Player> members = new CopyOnWriteArraySet<>();

    public SumoTeam(@NotNull String teamName, @NotNull TeamColor color) {
        this.teamName = teamName;
        this.color = color;
        this.scoreboardTeam = this.createScoreboardTeam();
    }

    public @NotNull Team getScoreboardTeam() {
        return this.scoreboardTeam;
    }

    private @NotNull Team createScoreboardTeam() {
        return MinecraftServer.getTeamManager().createBuilder(this.teamName)
                .teamColor(NamedTextColor.nearestTo(TextColor.color(this.color.getColor())))
                .collisionRule(TeamsPacket.CollisionRule.NEVER)
                .updateTeamPacket()
                .build();
    }

    public @NotNull TeamColor getColor() {
        return this.color;
    }

    public void setSuffix(@NotNull Component suffix) {
        this.scoreboardTeam.updateSuffix(suffix);
    }

    public void addMember(@NotNull Player member) {
        member.setTeam(this.scoreboardTeam);
        this.members.add(member);
    }

    public void removeMember(@NotNull Player member) {
        member.setTeam(null);
        this.members.remove(member);
    }

    public void destroy() {
        for (Player player : this.members) {
            player.setTeam(null);
        }

        this.members.clear();
        MinecraftServer.getTeamManager().deleteTeam(this.scoreboardTeam);
    }

    @Override
    public @NotNull Set<Player> getPlayers() {
        return this.members;
    }
}
