package dev.emortal.minestom.blocksumo;

import dev.emortal.api.model.gamedata.GameDataGameMode;
import dev.emortal.api.model.gamedata.V1BlockSumoPlayerData;
import dev.emortal.api.utils.GrpcStubCollection;
import dev.emortal.minestom.blocksumo.command.CreditsCommand;
import dev.emortal.minestom.blocksumo.command.GameCommand;
import dev.emortal.minestom.blocksumo.command.SaveLoadoutCommand;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapManager;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import dev.emortal.minestom.gamesdk.MinestomGameServer;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import dev.emortal.minestom.gamesdk.util.GamePlayerDataRepository;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Main {
    public static final @NotNull V1BlockSumoPlayerData DEFAULT_PLAYER_DATA = V1BlockSumoPlayerData.newBuilder()
            .setShearsSlot(0)
            .setBlockSlot(45)
            .build();

    public static void main(String[] args) {
        MinestomGameServer server = MinestomGameServer.create(() -> {
            MapManager mapManager = new MapManager();

            GamePlayerDataRepository<V1BlockSumoPlayerData> playerStorage = new GamePlayerDataRepository<>(
                    GrpcStubCollection.getGamePlayerDataService().orElse(null), DEFAULT_PLAYER_DATA,
                    V1BlockSumoPlayerData.class, GameDataGameMode.BLOCK_SUMO
            );

            return GameSdkConfig.builder()
                    .minPlayers(BlockSumoGame.MIN_PLAYERS)
                    .gameCreator(info -> {
                        Map<UUID, V1BlockSumoPlayerData> playerData;
                        try {
                            playerData = playerStorage.getPlayerData(info.playerIds());
                        } catch (Exception e) {
                            playerData = info.playerIds().stream().collect(Collectors.toMap(id -> id, id -> DEFAULT_PLAYER_DATA));
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
