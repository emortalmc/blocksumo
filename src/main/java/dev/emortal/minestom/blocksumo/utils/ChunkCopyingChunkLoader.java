package dev.emortal.minestom.blocksumo.utils;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class ChunkCopyingChunkLoader implements IChunkLoader {

    private final @NotNull Instance source;

    public ChunkCopyingChunkLoader(@NotNull Instance source) {
        this.source = source;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        Chunk sourceChunk = this.source.getChunk(chunkX, chunkZ);
        if (sourceChunk == null) return CompletableFuture.completedFuture(null);

        Chunk copy = sourceChunk.copy(instance, chunkX, chunkZ);
        return CompletableFuture.completedFuture(copy);
    }

    @Override
    public boolean supportsParallelLoading() {
        return true;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        throw new UnsupportedOperationException("Cannot save chunks with this loader!");
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunks(@NotNull Collection<Chunk> chunks) {
        throw new UnsupportedOperationException("Cannot save chunks with this loader!");
    }
}