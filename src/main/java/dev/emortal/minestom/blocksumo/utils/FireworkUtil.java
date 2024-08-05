package dev.emortal.minestom.blocksumo.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public final class FireworkUtil {

    public static void showFirework(@NotNull Collection<Player> players, @NotNull Instance instance, @NotNull Pos pos,
                                    @NotNull List<FireworkExplosion> effects) {
        ItemStack item = ItemStack.builder(Material.FIREWORK_ROCKET).set(ItemComponent.FIREWORKS, new FireworkList((byte) 0, effects)).build();

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
