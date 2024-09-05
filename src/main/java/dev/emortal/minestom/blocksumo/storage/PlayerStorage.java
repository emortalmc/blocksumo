package dev.emortal.minestom.blocksumo.storage;

import dev.emortal.api.model.gamedata.GameDataGameMode;
import dev.emortal.api.model.gamedata.V1BlockSumoPlayerData;
import dev.emortal.api.service.gameplayerdata.GamePlayerDataService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerStorage {
    public static final @NotNull V1BlockSumoPlayerData DEFAULT_DATA = V1BlockSumoPlayerData.newBuilder()
            .setShearsSlot(0)
            .setBlockSlot(45)
            .build();

    private final @Nullable GamePlayerDataService gamePlayerDataService;

    public PlayerStorage(@Nullable GamePlayerDataService gamePlayerDataService) {
        this.gamePlayerDataService = gamePlayerDataService;
    }

    public @NotNull V1BlockSumoPlayerData getPlayerData(@NotNull UUID playerId) {
        if (this.gamePlayerDataService == null) return DEFAULT_DATA;

        V1BlockSumoPlayerData playerData = this.gamePlayerDataService.getGameData(GameDataGameMode.BLOCK_SUMO, V1BlockSumoPlayerData.class, playerId);
        return playerData != null ? playerData : DEFAULT_DATA;
    }

    public @NotNull Map<UUID, V1BlockSumoPlayerData> getPlayerData(@NotNull Set<UUID> playerIds) {
        if (this.gamePlayerDataService == null)
            return playerIds.stream().collect(Collectors.toMap(id -> id, id -> DEFAULT_DATA));

        Map<UUID, V1BlockSumoPlayerData> responseData = this.gamePlayerDataService
                .getGameData(GameDataGameMode.BLOCK_SUMO, V1BlockSumoPlayerData.class, playerIds);

        for (Map.Entry<UUID, V1BlockSumoPlayerData> entry : responseData.entrySet()) {
            if (entry.getValue() == null) {
                responseData.put(entry.getKey(), DEFAULT_DATA);
            }
        }

        return responseData;
    }
}
