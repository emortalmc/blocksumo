package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class PortAFort extends PowerUp {
    private static final Component NAME = Component.text("Port-A-Fort", NamedTextColor.GOLD);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.CRAFTING_TABLE, NAME, ItemRarity.LEGENDARY);

    private final BlockSumoGame game;
    public PortAFort(@NotNull BlockSumoGame game) {
        super(game, "portafort", ITEM_INFO, SpawnLocation.CENTER);
        this.game = game;
    }

    @Override
    public boolean shouldHandleBlockPlace() {
        return true;
    }

    @Override
    public void onBlockPlace(@NotNull Player player, @NotNull Player.Hand hand, @NotNull Point clickedPos) {
        this.removeOneItemFromPlayer(player, hand);

        Block woolBlock = player.getTag(PlayerTags.TEAM_COLOR).getWoolItem().material().registry().block();

        var middleLayers = 4;

        AbsoluteBlockBatch bottomLayer = new AbsoluteBlockBatch();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                bottomLayer.setBlock(clickedPos.add(x, -1, z), woolBlock);
            }
        }
        RelativeBlockBatch middleLayer = new RelativeBlockBatch();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x != 2 && x != -2 && z != 2 && z != -2) continue;
                middleLayer.setBlock(x, 0, z, woolBlock);
            }
        }
        AbsoluteBlockBatch topLayer = new AbsoluteBlockBatch();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (x != 3 && x != -3 && z != 3 && z != -3) continue;
                if ((x == 3 && z == 3) || (x == 3 && z == -3) || (x == -3 && z == -3)|| (x == -3 && z == 3)) continue; // Remove corners
                topLayer.setBlock(clickedPos.add(x, middleLayers, z), woolBlock);

                if (x % 2 == 0 || z % 2 == 0) topLayer.setBlock(clickedPos.add(x, middleLayers + 1, z), woolBlock);
            }
        }

        this.game.getInstance().scheduler().submitTask(new Supplier<>() {
            int y = 0;
            @Override
            public TaskSchedule get() {
                y++;
                if (y == 1) {
                    bottomLayer.apply(game.getInstance(), null);
                }
                if (y > 1 && y < middleLayers + 2) {
                    middleLayer.apply(game.getInstance(), clickedPos.add(0, y - 2, 0), null);
                }
                if ((clickedPos.blockY() + y) > 77) { // height limit
                    game.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1f, 0.8f), clickedPos.add(0, y - 1, 0));
                    return TaskSchedule.stop();
                }
                if (y == middleLayers + 3) {
                    topLayer.apply(game.getInstance(), null);
                    game.playSound(Sound.sound(SoundEvent.BLOCK_ANVIL_PLACE, Sound.Source.MASTER, 0.4f, 2f), clickedPos.add(0, y - 1, 0));
                    game.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1f, 1.2f), clickedPos.add(0, y - 1, 0));

                    return TaskSchedule.stop();
                }

                game.playSound(Sound.sound(SoundEvent.BLOCK_ANVIL_PLACE, Sound.Source.MASTER, 0.4f, 1.2f + (y / 10f)), clickedPos.add(0, y - 1, 0));

                return TaskSchedule.tick(3);
            }
        });
        bottomLayer.unsafeApply(this.game.getInstance(), null);
    }
}
