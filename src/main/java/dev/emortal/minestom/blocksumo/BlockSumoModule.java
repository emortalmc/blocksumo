package dev.emortal.minestom.blocksumo;

import dev.emortal.minestom.blocksumo.command.GameCommand;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.map.MapManager;
import dev.emortal.minestom.core.module.Module;
import dev.emortal.minestom.core.module.ModuleData;
import dev.emortal.minestom.core.module.ModuleEnvironment;
import dev.emortal.minestom.core.module.permissions.PermissionModule;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.MojangAuth;
import org.jetbrains.annotations.NotNull;

@ModuleData(name = "blocksumo", softDependencies = {GameSdkModule.class, PermissionModule.class}, required = false)
public class BlockSumoModule extends Module {
    private final MapManager mapManager;

    protected BlockSumoModule(@NotNull ModuleEnvironment environment) {
        super(environment);
        this.mapManager = new MapManager();

        GameSdkModule.init(
                new GameSdkConfig.Builder()
                        .minPlayers(1)
                        .gameSupplier((info, eventNode) -> new BlockSumoGame(info, eventNode, this.mapManager.getRandomMap()))
                        .maxGames(5)
                        .build()
        );

        MojangAuth.init();

//        GameSdkModule.getGameManager().addGame(this.createGame(
//                new GameCreationInfo(
//                        Set.of(UUID.fromString("8d36737e-1c0a-4a71-87de-9906f577845e")),
//                        Instant.now()
//                )
//        ));

        MinecraftServer.getCommandManager().register(new GameCommand());
    }

    @Override
    public boolean onLoad() {
        return false;
    }

    @Override
    public void onUnload() {

    }
}
