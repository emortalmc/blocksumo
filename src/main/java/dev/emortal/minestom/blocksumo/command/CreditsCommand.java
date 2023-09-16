package dev.emortal.minestom.blocksumo.command;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.gamesdk.game.Game;
import dev.emortal.minestom.gamesdk.game.GameProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreditsCommand extends Command {

    private static final @NotNull Component UNKNOWN_MESSAGE = Component.text("We're not sure who made this map", NamedTextColor.RED);
    private final @NotNull GameProvider gameProvider;

    public CreditsCommand(@NotNull GameProvider gameProvider) {
        super("credits");

        this.gameProvider = gameProvider;

        setDefaultExecutor((sender, context) -> {
            BlockSumoGame game = getGame(sender);
            if (game == null) return;

            String[] mapUsernames = game.getMapData().credits().split("\n");

            if (mapUsernames.length == 0) {
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
        });
    }

    private @Nullable BlockSumoGame getGame(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command!");
            return null;
        }

        Game game = gameProvider.findGame(player);
        if (game == null) {
            sender.sendMessage("You are not in a game!");
            return null;
        }

        return (BlockSumoGame) game;
    }
}
