package dev.emortal.minestom.blocksumo.command;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.event.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.game.event.EventRegistry;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.game.Game;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

import java.util.Optional;

public class GameCommand extends Command {

    public GameCommand() {
        super("game");

        this.setCondition((sender, commandString) -> {
            return sender.hasPermission("command.game.blocksumo");
        });

        ArgumentLiteral start = new ArgumentLiteral("start");
        ArgumentLiteral event = new ArgumentLiteral("event");
        ArgumentWord eventType = new ArgumentWord("eventType").from(EventRegistry.EVENTS.keySet().toArray(new String[0]));

        this.addSyntax(this::executeStartEvent, start, event);
        this.addSyntax(this::executeStartEvent, start, event, eventType);
    }

    private void executeStartEvent(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command!");
            return;
        }
        String eventName = context.has("eventType") ? context.get("eventType") : null;

        Optional<Game> optionalGame = GameSdkModule.getGameManager().findGame(player);
        if (optionalGame.isEmpty()) {
            sender.sendMessage("You are not in a game!");
            return;
        }

        BlockSumoGame game = (BlockSumoGame) optionalGame.get();
        BlockSumoEvent event;
        if (eventName == null) event = EventRegistry.randomEvent().apply(game);
        else event = EventRegistry.EVENTS.get(eventName).apply(game);

        sender.sendMessage("Started event " + event.getClass().getSimpleName() + "...");
        // TODO: Set current event in game
    }
}
