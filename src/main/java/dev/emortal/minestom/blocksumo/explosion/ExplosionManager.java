package dev.emortal.minestom.blocksumo.explosion;

import dev.emortal.minestom.blocksumo.explosion.ExplosionData;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.team.TeamColor;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ExplosionManager {

    private final BlockSumoGame game;

    public ExplosionManager(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public @NotNull Entity spawnTnt(@NotNull Point position, int fuseTime, @NotNull ExplosionData explosion, @Nullable Player placer) {
        final Entity tnt = new Entity(EntityType.TNT);
        final PrimedTntMeta meta = (PrimedTntMeta) tnt.getEntityMeta();
        meta.setFuseTime(fuseTime);

        tnt.setInstance(game.getInstance(), position);
        playPrimedSound(tnt);

        scheduleExplosion(tnt, fuseTime, explosion, placer);
        return tnt;
    }

    private void playPrimedSound(@NotNull Entity tnt) {
        final Sound sound = Sound.sound(SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, 2, 1);
        game.getAudience().playSound(sound, tnt);
    }

    private void scheduleExplosion(@NotNull Entity tnt, int fuseTime, @NotNull ExplosionData explosion, @Nullable Player placer) {
        tnt.scheduler().buildTask(() -> {
            explode(tnt.getPosition(), explosion, placer, tnt);
            tnt.remove();
        }).delay(TaskSchedule.tick(fuseTime)).schedule();
    }

    public void explode(@NotNull Point position, @NotNull ExplosionData explosion, @Nullable Player source, @NotNull Entity entity) {
        doExplosionDamage(position, explosion, source, entity);

        final float posX = (float) position.x();
        final float posY = (float) position.y();
        final float posZ = (float) position.z();
        game.getInstance().sendGroupedPacket(new ExplosionPacket(posX, posY, posZ, explosion.size(), new byte[0], 0, 0, 0));

        if (!explosion.breakBlocks()) return;
        explodeBlocks(position, explosion);
    }

    private void doExplosionDamage(@NotNull Point position, @NotNull ExplosionData explosion, @Nullable Player source,
                                   @NotNull Entity entity) {
        final UUID sourceId = source == null ? null : source.getUuid();
        final TeamColor sourceColor = source == null ? null : source.getTag(PlayerTags.TEAM_COLOR);

        final double forceDistance = explosion.forceDistance() * explosion.forceDistance();
        for (final Player player : game.getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) continue;

            if (!player.getUuid().equals(sourceId)) {
                // TODO: Check spawn protection
                if (player.getTag(PlayerTags.TEAM_COLOR) == sourceColor) continue;
                // TODO: Check for anti-knockback
            }

            final double distance = player.getPosition().distanceSquared(position);
            if (distance > forceDistance) continue;

            player.damage(DamageType.fromEntity(entity), 0);
            final Vec newVelocity = player.getPosition()
                    .sub(position.sub(0, 1, 0))
                    .asVec()
                    .normalize()
                    .mul(explosion.force());
            player.setVelocity(newVelocity);
        }
    }

    private void explodeBlocks(@NotNull Point position, @NotNull ExplosionData explosion) {
        final AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
        final List<Point> blocksToBreak = getBlocksInSphere(explosion.size());

        for (final Point pos : blocksToBreak) {
            final Point blockPos = position.add(pos);
            final Block block = game.getInstance().getBlock(blockPos);
            if (!block.name().toLowerCase(Locale.ROOT).contains("wool") && !block.isAir()) continue;

            // Send block break effect because Minestom won't send it for a batch break.
            game.getInstance().sendGroupedPacket(new EffectPacket(2001, blockPos, block.stateId(), false));

            batch.setBlock(blockPos, Block.AIR);
        }

        batch.apply(game.getInstance(), () -> {});
    }

    private static @NotNull List<Point> getBlocksInSphere(final int radius) {
        final List<Point> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <=radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    final int distance = x * x + y * y + z * z;
                    if (distance > radius * radius) continue;

                    blocks.add(new Vec(x, y, z));
                }
            }
        }
        return blocks;
    }
}
