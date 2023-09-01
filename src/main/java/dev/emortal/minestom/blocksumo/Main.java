package dev.emortal.minestom.blocksumo;

import dev.emortal.minestom.blocksumo.command.GameCommand;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapManager;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import net.minestom.server.MinecraftServer;

public class Main {
    public static void main(String[] args) {
        MinestomGameServer server = MinestomGameServer.create(() -> {
            MapManager mapManager = new MapManager();

            return GameSdkConfig.builder()
                    .minPlayers(BlockSumoGame.MIN_PLAYERS)
                    .maxGames(5)
                    .gameCreator(info -> new BlockSumoGame(info, mapManager.getMap(info.mapId())))
                    .build();
        });

        MinecraftServer.getCommandManager().register(new GameCommand(server.getGameProvider()));
    }
}