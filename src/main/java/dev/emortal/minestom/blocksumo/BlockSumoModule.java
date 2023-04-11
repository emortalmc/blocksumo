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
import org.jetbrains.annotations.NotNull;

@ModuleData(name = "blocksumo", softDependencies = {GameSdkModule.class, PermissionModule.class}, required = false)
public class BlockSumoModule extends Module {
    public static final int MIN_PLAYERS = 2;

    protected BlockSumoModule(@NotNull ModuleEnvironment environment) {
        super(environment);

        MapManager mapManager = new MapManager();

        GameSdkModule.init(
                new GameSdkConfig.Builder()
                        .minPlayers(MIN_PLAYERS)
                        .gameSupplier((info, eventNode) -> new BlockSumoGame(info, eventNode, mapManager.getMap(info.mapId())))
                        .maxGames(5)
                        .build()
        );

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
