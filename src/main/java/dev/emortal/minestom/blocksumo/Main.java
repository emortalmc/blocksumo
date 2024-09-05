package dev.emortal.minestom.blocksumo;

import dev.emortal.api.model.gamedata.V1BlockSumoPlayerData;
import dev.emortal.api.utils.GrpcStubCollection;
import dev.emortal.minestom.blocksumo.command.CreditsCommand;
import dev.emortal.minestom.blocksumo.command.GameCommand;
import dev.emortal.minestom.blocksumo.command.SaveLoadoutCommand;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapManager;
import dev.emortal.minestom.blocksumo.storage.PlayerStorage;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import net.minestom.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Main {

    public static void main(String[] args) {
        MinestomGameServer server = MinestomGameServer.create(() -> {
            MapManager mapManager = new MapManager();

            PlayerStorage playerStorage = new PlayerStorage(GrpcStubCollection.getGamePlayerDataService().orElse(null));

            return GameSdkConfig.builder()
                    .minPlayers(BlockSumoGame.MIN_PLAYERS)
                    .gameCreator(info -> {
                        Map<UUID, V1BlockSumoPlayerData> playerData;
                        try {
                            playerData = playerStorage.getPlayerData(info.playerIds());
                        } catch (Exception e) {
                            playerData = info.playerIds().stream().collect(Collectors.toMap(id -> id, id -> PlayerStorage.DEFAULT_DATA));
                        }

                        return new BlockSumoGame(info, mapManager.getMap(info.mapId()), playerData);
                    })
                    .build();
        });

        MessagingModule messagingModule = server.getModuleManager().getModule(MessagingModule.class);

        MinecraftServer.getCommandManager().register(new GameCommand(server.getGameProvider()));
        MinecraftServer.getCommandManager().register(new CreditsCommand(server.getGameProvider()));

        if (messagingModule != null && messagingModule.getKafkaProducer() != null) {
            MinecraftServer.getCommandManager().register(new SaveLoadoutCommand(server.getGameProvider(), messagingModule.getKafkaProducer()));
        } else {
            System.out.println("Messaging module not found, not registering /saveloadout command");
        }
    }
}
