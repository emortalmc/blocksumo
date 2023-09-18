package dev.emortal.minestom.blocksumo.command;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.gamesdk.game.Game;
import dev.emortal.minestom.gamesdk.game.GameProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CreditsCommand extends Command {
    private static final @NotNull Component UNKNOWN_MESSAGE = Component.text("We're not sure who made this map", NamedTextColor.RED);

    private final @NotNull GameProvider gameProvider;

    public CreditsCommand(@NotNull GameProvider gameProvider) {
        super("credits");
        this.gameProvider = gameProvider;

        super.setCondition(Conditions::playerOnly);
        super.setDefaultExecutor(this::execute);
    }

    private @Nullable BlockSumoGame getGame(@NotNull CommandSender sender) {
        Game game = this.gameProvider.findGame((Player) sender);
        if (game == null) {
            sender.sendMessage("You are not in a game!");
            return null;
        }

        return (BlockSumoGame) game;
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        BlockSumoGame game = this.getGame(sender);
        if (game == null) return;

        List<String> mapUsernames = game.mapData().credits();
        if (mapUsernames.isEmpty()) {
            sender.sendMessage(UNKNOWN_MESSAGE);
            return;
        }

        TextComponent.Builder message = Component.text();
        message.append(Component.text("This map was created by:", NamedTextColor.LIGHT_PURPLE));

        for (String mapUsername : mapUsernames) {
            message.append(Component.newline());
            message.append(Component.text(" - "));
            message.append(Component.text(mapUsername));
        }

        sender.sendMessage(message.build());
    }
}
