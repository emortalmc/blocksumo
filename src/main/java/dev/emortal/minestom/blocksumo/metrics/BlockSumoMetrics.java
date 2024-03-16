package dev.emortal.minestom.blocksumo.metrics;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockSumoMetrics {
    private final @NotNull BlockSumoGame game;
    private final @NotNull String gameId;

    public BlockSumoMetrics(@NotNull BlockSumoGame game) {
        this.game = game;
        this.gameId = game.getCreationInfo().id();
    }

    public Set<Meter> register(@NotNull MeterRegistry meterRegistry) {
        Counter blocksPlaced = Counter.builder("blocksumo.blocks_placed")
                .description("The number of blocks placed by players in a game")
                .tags("gameId", this.gameId)
                .register(meterRegistry);

        Counter blocksDestroyed = Counter.builder("blocksumo.blocks_destroyed")
                .description("The number of blocks destroyed by players in a game")
                .tags("gameId", this.gameId)
                .register(meterRegistry);

        game.getEventNode().addListener(PlayerBlockPlaceEvent.class, event -> {
            blocksPlaced.increment();
        }).addListener(PlayerBlockBreakEvent.class, event -> {
            blocksDestroyed.increment();
        });

        Gauge livesGauge = Gauge.builder("blocksumo.lives", this::getTotalLives)
                .description("The total number of lives remaining in a game")
                .tags("gameId", this.gameId)
                .register(meterRegistry);

        Gauge playersAliveGauge = Gauge.builder("blocksumo.players_alive", this::getPlayersAlive)
                .description("The number of players still alive in a game")
                .tags("gameId", this.gameId)
                .register(meterRegistry);

        return Set.of(blocksPlaced, blocksDestroyed, livesGauge, playersAliveGauge);
    }

    private int getTotalLives() {
        return this.game.getPlayers().stream()
                .map(player -> (int) player.getTag(PlayerTags.LIVES))
                .reduce(0, Integer::sum);
    }

    private int getPlayersAlive() {
        return (int) this.game.getPlayers().stream()
                .filter(player -> player.getTag(PlayerTags.LIVES) > 0)
                .count();
    }
}
