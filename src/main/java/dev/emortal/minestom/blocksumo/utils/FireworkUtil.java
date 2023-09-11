package dev.emortal.minestom.blocksumo.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FireworkRocketMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.firework.FireworkEffect;
import net.minestom.server.item.metadata.FireworkMeta;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public final class FireworkUtil {

    public static void showFirework(@NotNull Collection<Player> players, @NotNull Instance instance, @NotNull Pos pos,
                                    @NotNull List<FireworkEffect> effects) {
        ItemMeta itemMeta = new FireworkMeta.Builder().effects(effects).build();
        ItemStack item = ItemStack.builder(Material.FIREWORK_ROCKET).meta(itemMeta).build();

        Entity firework = new Entity(EntityType.FIREWORK_ROCKET);
        FireworkRocketMeta meta = (FireworkRocketMeta) firework.getEntityMeta();
        meta.setFireworkInfo(item);

        firework.updateViewableRule(players::contains);
        firework.setNoGravity(true);
        firework.setInstance(instance, pos);

        PacketUtils.sendGroupedPacket(players, new EntityStatusPacket(firework.getEntityId(), (byte) 17));

        firework.remove();
    }

    private FireworkUtil() {
    }
}
