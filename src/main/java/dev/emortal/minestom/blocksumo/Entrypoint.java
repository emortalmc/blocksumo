package dev.emortal.minestom.blocksumo;

import dev.emortal.minestom.core.MinestomServer;
import dev.emortal.minestom.gamesdk.GameSdkModule;

public class Entrypoint {
    public static void main(String[] args) {
        new MinestomServer.Builder()
                .commonModules()
                .module(GameSdkModule.class, GameSdkModule::new)
                .module(BlockSumoModule.class, BlockSumoModule::new)
                .build();
    }
}