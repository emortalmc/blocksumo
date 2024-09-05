package dev.emortal.minestom.blocksumo.command;

import com.google.protobuf.Any;
import com.google.protobuf.util.FieldMaskUtil;
import dev.emortal.api.message.gamedata.UpdateGamePlayerDataMessage;
import dev.emortal.api.model.gamedata.GameDataGameMode;
import dev.emortal.api.model.gamedata.V1BlockSumoPlayerData;
import dev.emortal.api.utils.kafka.FriendlyKafkaProducer;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.gamesdk.game.GameProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

public class SaveLoadoutCommand extends Command {

    public SaveLoadoutCommand(@NotNull GameProvider gameProvider, @NotNull FriendlyKafkaProducer kafkaProducer) {
        super("saveloadout");

        this.setCondition(Conditions::playerOnly);
        this.setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            BlockSumoGame game = (BlockSumoGame) gameProvider.findGame(player);

            int shearsSlot = findShearsSlot(player);
            int woolSlot = findWoolSlot(player);

            if (shearsSlot == -1 || woolSlot == -1) {
                player.sendMessage(Component.text("You must be alive to save your loadout!", NamedTextColor.RED));
                return;
            }

            V1BlockSumoPlayerData playerData = game.getPlayerDataMap().get(player.getUuid());
            if (playerData.getBlockSlot() == woolSlot && playerData.getShearsSlot() == shearsSlot) {
                player.sendMessage(Component.text("Your loadout hasn't changed!", NamedTextColor.RED));
                return;
            }

            V1BlockSumoPlayerData newPlayerData = V1BlockSumoPlayerData.newBuilder()
                    .setBlockSlot(woolSlot)
                    .setShearsSlot(shearsSlot)
                    .build();

            // Update data in the existing game
            game.getPlayerDataMap().put(player.getUuid(), newPlayerData);
            // Update data in the DB
            kafkaProducer.produceAndForget(UpdateGamePlayerDataMessage.newBuilder()
                    .setGameMode(GameDataGameMode.BLOCK_SUMO)
                    .setPlayerId(player.getUuid().toString())
                    .setData(Any.pack(newPlayerData))
                    .setDataMask(FieldMaskUtil.fromStringList(Set.of("block_slot", "shears_slot")))
                    .build());

            player.sendMessage(Component.text("Loadout saved!", NamedTextColor.GREEN));
        });
    }

    private int findShearsSlot(@NotNull Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItemStack(i).material() == Material.SHEARS) {
                return i;
            }
        }

        return -1;
    }

    private int findWoolSlot(@NotNull Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItemStack(i).material().name().toUpperCase(Locale.ROOT).endsWith("_WOOL")) {
                return i;
            }
        }

        return -1;
    }


}
