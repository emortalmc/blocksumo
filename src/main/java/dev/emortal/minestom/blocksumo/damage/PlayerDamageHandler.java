package dev.emortal.minestom.blocksumo.damage;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import dev.emortal.minestom.blocksumo.game.PlayerTags;
import dev.emortal.minestom.blocksumo.game.SpawnProtectionManager;
import dev.emortal.minestom.blocksumo.powerup.PowerUp;
import dev.emortal.minestom.blocksumo.team.TeamColor;
import dev.emortal.minestom.blocksumo.utils.KnockbackUtil;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class PlayerDamageHandler {

    private final @NotNull BlockSumoGame game;

    public PlayerDamageHandler(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(EntityAttackEvent.class, this::onAttack);
        eventNode.addListener(EntityDamageEvent.class, this::onDamage);
    }

    private void onAttack(@NotNull EntityAttackEvent event) {
        Entity target = event.getTarget();
        if (target.getEntityType() == EntityType.FIREBALL) {
            this.normalizeFireballVelocity(target);
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player attacker)) return;
        if (!(target instanceof Player victim)) return;

        if (attacker.getGameMode() != GameMode.SURVIVAL) return;
        if (this.areOnSameTeam(attacker, victim)) return;

        if (this.game.getSpawnProtectionManager().isProtected(attacker)) {
            this.game.getSpawnProtectionManager().endProtection(attacker);
        }
        if (this.game.getSpawnProtectionManager().isProtected(victim)) {
            this.game.getSpawnProtectionManager().notifyProtected(attacker, victim);
            return;
        }

        if (!victim.getTag(PlayerTags.CAN_BE_HIT)) return;
        if (!this.withinLegalRange(attacker, victim)) return;
        victim.setTag(PlayerTags.CAN_BE_HIT, false);

        victim.damage(DamageType.fromPlayer(attacker), 0);
        this.game.sendGroupedPacket(new HitAnimationPacket(victim.getEntityId(), attacker.getPosition().yaw()));
        KnockbackUtil.takeKnockback(attacker, victim);

        PowerUp heldPowerUp = this.game.getPowerUpManager().getHeldPowerUp(attacker, Player.Hand.MAIN);
        if (heldPowerUp != null) heldPowerUp.onAttack(attacker, victim);

        victim.scheduler().buildTask(() -> victim.setTag(PlayerTags.CAN_BE_HIT, true)).delay(TaskSchedule.tick(10)).schedule();
    }

    private void onDamage(@NotNull EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        this.updateLastDamageTime(player);
    }

    private void normalizeFireballVelocity(@NotNull Entity entity) {
        entity.setVelocity(entity.getPosition().direction().mul(20.0));
    }

    private boolean areOnSameTeam(@NotNull Player player1, @NotNull Player player2) {
        return this.getTeamColor(player1) == this.getTeamColor(player2);
    }

    private boolean withinLegalRange(@NotNull Player attacker, @NotNull Player victim) {
        return attacker.getDistanceSquared(victim) <= 4.5 * 4.5;
    }

    private @NotNull TeamColor getTeamColor(@NotNull Player player) {
        return player.getTag(PlayerTags.TEAM_COLOR);
    }

    private void updateLastDamageTime(@NotNull Player player) {
        player.setTag(PlayerTags.LAST_DAMAGE_TIME, System.currentTimeMillis());
    }
}
