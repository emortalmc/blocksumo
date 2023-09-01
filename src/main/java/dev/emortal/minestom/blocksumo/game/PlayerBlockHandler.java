package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.MapData;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerBlockHandler {
    private static final Point SPAWN = MapData.CENTER.sub(0.5, 0, 0.5);

    private static final Direction[] DIRECTIONS = Direction.values();


    // These values are trial-and-error. If they break, blame me. Don't complain, just fix it kekw
    private static final float REACH_TOLERANCE = 1.0f;
    private static final float NON_SNEAKING_RANGE = 4.65f + REACH_TOLERANCE;
    private static final float SNEAKING_RANGE = 4.9f + REACH_TOLERANCE;

    private final BlockSumoGame game;
    private final Map<Point, Task> centerBlockBreakTasks = new ConcurrentHashMap<>();

    public PlayerBlockHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockPlaceEvent.class, this::onBlockPlace);

        // Fixes replacement of non-solid blocks like stairs
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (event.getInstance().getBlock(event.getBlockPosition(), Block.Getter.Condition.TYPE).isSolid()) {
                event.setCancelled(true);
            }
        });

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            Task removedTask = this.centerBlockBreakTasks.remove(event.getBlockPosition());
            if (removedTask != null) removedTask.cancel();

            final String blockName = event.getBlock().name().toLowerCase(Locale.ROOT);
            if (!blockName.contains("wool")) event.setCancelled(true);
        });
    }

    private void onBlockPlace(@NotNull PlayerBlockPlaceEvent event) {
        event.consumeBlock(false);

        final Player player = event.getPlayer();
        final Point blockPosition = event.getBlockPosition();

        if (nextToBarrier(blockPosition)) {
            event.setCancelled(true);
            // force player downwards when placing next to barriers - avoids clutching literally everything
            player.teleport(player.getPosition().sub(0, 1, 0));
            player.setVelocity(new Vec(0, -20, 0));
            return;
        }

        final PowerUp heldPowerup = game.getPowerUpManager().getHeldPowerUp(event.getPlayer(), event.getHand());

        if (!withinLegalRange(player, blockPosition)) {
            event.setCancelled(true);
            return;
        }

        // Handle powerups before height limit check. It can be aggravating
        if (heldPowerup != null) {
            handlePowerUp(heldPowerup, event);
            return;
        }

        // Exclude powerup usage from the height limit. It can be aggravating
        if (!withinWorldLimits(blockPosition)) {
            event.setCancelled(true);
            return;
        }

        if (isAroundCenter(blockPosition)) {
            scheduleCenterBlockBreak(blockPosition, event.getBlock());
        }
    }

    private boolean nextToBarrier(@NotNull Point blockPos) {
        for (Direction direction : DIRECTIONS) {
            if (game.getSpawningInstance().getBlock(blockPos.add(direction.normalX(), direction.normalY(), direction.normalZ()), Block.Getter.Condition.TYPE) == Block.BARRIER) {
                return true;
            }
        }

        return false;
    }

    private boolean withinLegalRange(@NotNull Player player, @NotNull Point blockPos) {
        float range = player.isSneaking() ? SNEAKING_RANGE : NON_SNEAKING_RANGE;

        return blockPos.distanceSquared(player.getPosition().add(0, 1, 0)) <= range * range;
    }

    private boolean withinWorldLimits(@NotNull Point blockPos) {
        return blockPos.y() <= 77 && blockPos.y() >= 51.5;
    }

    private boolean isAroundCenter(@NotNull Point blockPos) {
        return blockPos.distanceSquared(SPAWN) < 3 * 3 && blockPos.blockY() > (SPAWN.blockY() - 1);
    }

    private void scheduleCenterBlockBreak(@NotNull Point blockPos, @NotNull Block block) {
        final Task task = game.getSpawningInstance().scheduler().buildTask(() -> {
            game.getSpawningInstance().setBlock(blockPos, Block.AIR);
            sendBlockBreakEffect(blockPos, block);
        }).delay(TaskSchedule.seconds(5)).schedule();
        centerBlockBreakTasks.put(blockPos, task);
    }

    private void sendBlockBreakEffect(@NotNull Point blockPos, @NotNull Block block) {
        final EffectPacket packet = new EffectPacket(2001, blockPos, block.stateId(), false);
        game.sendGroupedPacket(packet);
    }

    private boolean handlePowerUp(@Nullable PowerUp powerUp, @NotNull PlayerBlockPlaceEvent event) {
        if (powerUp == null) return false;
        if (!powerUp.shouldHandleBlockPlace()) return false;

        event.setCancelled(true);
        powerUp.onBlockPlace(event.getPlayer(), event.getHand(), event.getBlockPosition().add(0.5, 0.1, 0.5));
        return true;
    }
}
