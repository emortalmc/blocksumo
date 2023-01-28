package dev.emortal.minestom.blocksumo.command;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.event.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.event.EventManager;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class GameCommand extends Command {

    public GameCommand() {
        super("game");

        this.setCondition((sender, commandString) -> {
            return sender.hasPermission("command.game.blocksumo");
        });

        ArgumentLiteral start = new ArgumentLiteral("start");
        ArgumentLiteral event = new ArgumentLiteral("event");
        ArgumentWord eventType = new ArgumentWord("eventType");
        eventType.setSuggestionCallback((sender, context, suggestion) -> suggestEvents(sender, suggestion));

        this.addSyntax(this::executeStartEvent, start, event);
        this.addSyntax(this::executeStartEvent, start, event, eventType);
    }

    private @Nullable BlockSumoGame getGame(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command!");
            return null;
        }

        final Optional<Game> game = GameSdkModule.getGameManager().findGame(player);
        if (game.isEmpty()) {
            sender.sendMessage("You are not in a game!");
            return null;
        }

        return (BlockSumoGame) game.get();
    }

    private void suggestEvents(final CommandSender sender, final Suggestion suggestion) {
        final BlockSumoGame game = getGame(sender);
        if (game == null) return;

        for (final String eventName : game.getEventManager().getEventNames()) {
            suggestion.addEntry(new SuggestionEntry(eventName));
        }
    }

    private void executeStartEvent(CommandSender sender, CommandContext context) {
        final BlockSumoGame game = getGame(sender);
        if (game == null) return;

        final String eventName = context.has("eventType") ? context.get("eventType") : null;
        final EventManager eventManager = game.getEventManager();

        final BlockSumoEvent event;
        if (eventName != null) {
            event = eventManager.findNamedEvent(eventName);
            if (event == null) {
                sender.sendMessage(Component.text("Could not find event with name " + eventName + "!", NamedTextColor.RED));
                return;
            }
        } else {
            event = eventManager.findRandomEvent();
        }

        eventManager.startEvent(event);
        sender.sendMessage(Component.text("Started event " + event.getClass().getSimpleName() + "!", NamedTextColor.GREEN));
        // TODO: Set current event in game
    }
}
