package dev.emortal.minestom.blocksumo.utils;

import net.hollowcube.polar.PolarLoader;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class NoSaveyPolar extends PolarLoader {
    public NoSaveyPolar(@NotNull Path path) throws IOException {
        super(path);
    }

    @Override
    public void unloadChunk(Chunk chunk) {

    }
}
