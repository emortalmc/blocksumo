package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.MapData;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerBlockHandler {
    private static final Point SPAWN = MapData.CENTER.sub(0.5, 0, 0.5);

    private final BlockSumoGame game;
    private final Map<Point, Task> centerBlockBreakTasks = new ConcurrentHashMap<>();

    public PlayerBlockHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            event.consumeBlock(false);

            final Player player = event.getPlayer();
            final Point blockPosition = event.getBlockPosition();

            if (!withinLegalRange(player, blockPosition) || !withinWorldLimits(blockPosition)) {
                event.setCancelled(true);
                return;
            }

            final PowerUp heldItem = game.getPowerUpManager().getHeldPowerUp(event.getPlayer(), event.getHand());
            if (handlePowerUp(heldItem, event)) return;

            if (isAroundCenter(blockPosition)) {
                scheduleCenterBlockBreak(blockPosition, event.getBlock());
            }
        });

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            final String blockName = event.getBlock().name().toLowerCase(Locale.ROOT);
            if (!blockName.contains("wool")) event.setCancelled(true);
        });
    }

    private boolean withinLegalRange(@NotNull Player player, @NotNull Point blockPos) {
        return blockPos.distanceSquared(player.getPosition().add(0, 1, 0)) <= 5 * 5;
    }

    private boolean withinWorldLimits(@NotNull Point blockPos) {
        return blockPos.y() <= 77 && blockPos.y() >= 51.5;
    }

    private boolean isAroundCenter(@NotNull Point blockPos) {
        return blockPos.distanceSquared(SPAWN) < 3 * 3 && blockPos.blockY() > (SPAWN.blockY() - 1);
    }

    private void scheduleCenterBlockBreak(@NotNull Point blockPos, @NotNull Block block) {
        final Task task = game.getInstance().scheduler().buildTask(() -> {
            game.getInstance().setBlock(blockPos, Block.AIR);
            sendBlockBreakEffect(blockPos, block);
        }).delay(TaskSchedule.seconds(5)).schedule();
        centerBlockBreakTasks.put(blockPos, task);
    }

    private void sendBlockBreakEffect(@NotNull Point blockPos, @NotNull Block block) {
        final EffectPacket packet = new EffectPacket(2001, blockPos, block.stateId(), false);
        game.getInstance().sendGroupedPacket(packet);
    }

    private boolean handlePowerUp(@Nullable PowerUp powerUp, @NotNull PlayerBlockPlaceEvent event) {
        if (powerUp == null) return false;
        if (!powerUp.shouldHandleBlockPlace()) return false;

        event.setCancelled(true);
        powerUp.onBlockPlace(event.getPlayer(), event.getHand(), event.getBlockPosition().add(0.5, 0.1, 0.5));
        return true;
    }
}
