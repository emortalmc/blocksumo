package dev.emortal.minestom.blocksumo.powerup.item;

import dev.emortal.minestom.blocksumo.entity.BetterEntityProjectile;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.powerup.ItemRarity;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.powerup.PowerUpItemInfo;
import dev.emortal.minestom.blocksumo.powerup.SpawnLocation;
import dev.emortal.minestom.blocksumo.utils.KnockbackUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.SnowballMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class HotPotato extends PowerUp {
    private static final Component NAME = Component.text("Hot Potato", NamedTextColor.RED);
    private static final PowerUpItemInfo ITEM_INFO = new PowerUpItemInfo(Material.BAKED_POTATO, NAME, ItemRarity.IMPOSSIBLE, 1);
    private static final ItemStack POTATO_ITEM = ItemStack.of(Material.BAKED_POTATO);

    public static final Tag<Boolean> HOT_POTATO_HOLDER_TAG = Tag.Boolean("hotPotatoHolder");

    public HotPotato(@NotNull BlockSumoGame game) {
        super(game, "hotpotato", ITEM_INFO, SpawnLocation.NOWHERE);
    }

    @Override
    public void onUse(@NotNull Player player, @NotNull Player.Hand hand) {
        this.removeOneItemFromPlayer(player, hand);
        this.shootProjectile(player);
        this.playThrowSound(player);
    }

    private void shootProjectile(@NotNull Player shooter) {
        HotPotatoEntity snowball = new HotPotatoEntity(shooter);

        Instance instance = shooter.getInstance();
        snowball.scheduleRemove(10, TimeUnit.SECOND);
        snowball.setInstance(instance, shooter.getPosition().add(0, shooter.getEyeHeight(), 0));
    }

    private void playThrowSound(@NotNull Player thrower) {
        Sound sound = Sound.sound(SoundEvent.ENTITY_BLAZE_SHOOT, Sound.Source.BLOCK, 1, 1);
        Pos source = thrower.getPosition();
        this.game.playSound(sound, source.x(), source.y(), source.z());
    }

    public void sendWarningMessage(@NotNull Player player) {
        player.showTitle(Title.title(
                Component.empty(),
                Component.text("You have the hot potato!", NamedTextColor.RED),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(2000), Duration.ofMillis(250))
        ));
        player.playSound(Sound.sound(SoundEvent.BLOCK_BELL_USE, Sound.Source.MASTER, 1f, 0.3f), Sound.Emitter.self());

        this.game.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 0.3f), Sound.Emitter.self());
        this.game.sendMessage(
                Component.text()
                        .append(Component.text("!", NamedTextColor.RED, TextDecoration.BOLD))
                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(player.getUsername(), NamedTextColor.RED))
                        .append(Component.text(" has the hot potato!", NamedTextColor.GRAY))
        );
    }

    private final class HotPotatoEntity extends BetterEntityProjectile {
        private final Player shooter;
        public HotPotatoEntity(@NotNull Player shooter) {
            super(shooter, EntityType.SNOWBALL);

            this.shooter = shooter;

            ((SnowballMeta)entityMeta).setItem(POTATO_ITEM);
            entityMeta.setOnFire(true);

            setTag(PowerUp.NAME, HotPotato.super.name);
            setBoundingBox(0.25, 0.25, 0.25);
            setVelocity(shooter.getPosition().direction().mul(30.0));
        }

        @Override
        public void collidePlayer(@NotNull Point pos, @NotNull Player player) {
            this.shooter.removeTag(HOT_POTATO_HOLDER_TAG);
            player.setTag(HOT_POTATO_HOLDER_TAG, true);

            sendWarningMessage(player);
            KnockbackUtil.takeKnockback(player, this.position.direction(), 0.2);
            HotPotato.this.game.getPowerUpManager().givePowerUp(player, HotPotato.this);

            remove();
        }

        @Override
        public void collideBlock(@NotNull Point pos) {
            HotPotato.this.game.getPowerUpManager().givePowerUp(this.shooter, HotPotato.this);
            remove();
        }

        @Override
        public void tick(long time) {
            super.tick(time);
            if (this.position.y() < 45) {
                collideBlock(this.position);
            }
        }
    }
}
