package dev.emortal.minestom.blocksumo.team;

import dev.emortal.minestom.blocksumo.game.PlayerTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class PlayerTeamManager {
    private static final List<TeamColor> COLORS = List.of(TeamColor.values());
    private static final Random RANDOM = new Random();

    private final List<TeamColor> remainingColors = new ArrayList<>(COLORS);
    private final List<SumoTeam> teams = new ArrayList<>();

    public void allocateTeam(@NotNull Player player) {
        final TeamColor allocatedColor = allocateTeamColor();
        final SumoTeam team = new SumoTeam(allocatedColor.toString().toLowerCase(Locale.ROOT), allocatedColor);
        teams.add(team);

        updateTeamSuffix(team);
        addTeamColorToName(player, allocatedColor);
        player.setTag(PlayerTags.TEAM_COLOR, allocatedColor);
    }

    private void updateTeamSuffix(@NotNull SumoTeam team) {
        team.setSuffix(Component.text()
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(0, NamedTextColor.GREEN, TextDecoration.BOLD))
                .build());
    }

    private void addTeamColorToName(@NotNull Player player, @NotNull TeamColor color) {
        final Component displayName = Component.text()
                .append(Component.text(player.getUsername(), TextColor.color(color.getColor())))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("5", NamedTextColor.GREEN, TextDecoration.BOLD))
                .build();
        player.setDisplayName(displayName);
    }

    private @NotNull TeamColor allocateTeamColor() {
        // We will try to allocate all the colours before we start reusing them.
        if (remainingColors.isEmpty()) return getRandomColor();

        final int randomIndex = RANDOM.nextInt(remainingColors.size());
        final TeamColor selectedColor = remainingColors.get(randomIndex);
        remainingColors.remove(randomIndex);
        return selectedColor;
    }

    private @NotNull TeamColor getRandomColor() {
        final int randomIndex = RANDOM.nextInt(COLORS.size());
        return COLORS.get(randomIndex);
    }
}
