package dev.emortal.minestom.blocksumo.damage;

import dev.emortal.minestom.blocksumo.game.PlayerTags;
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
import org.jetbrains.annotations.NotNull;

public final class PlayerDamageHandler {

    public void registerListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(EntityAttackEvent.class, event -> {
            final Entity target = event.getTarget();
            if (target.getEntityType() == EntityType.FIREBALL) normalizeFireballVelocity(target);

            final Entity entity = event.getEntity();
            if (!(entity instanceof Player attacker)) return;
            if (!(target instanceof Player victim)) return;

            if (attacker.getGameMode() != GameMode.SURVIVAL) return;
            if (areOnSameTeam(attacker, victim)) return;
            if (!withinLegalRange(attacker, victim)) return;

            victim.damage(DamageType.fromPlayer(attacker), 0);
            KnockbackUtil.takeKnockback(attacker, victim); // TODO: Check for anti-KB tag when anti-KB command exists

            // TODO: Handle power-ups
        });

        eventNode.addListener(EntityDamageEvent.class, event -> {
            final Entity entity = event.getEntity();
            if (!(entity instanceof Player player)) return;
            updateLastDamageTime(player);
        });
    }

    private void normalizeFireballVelocity(@NotNull Entity entity) {
        entity.setVelocity(entity.getPosition().direction().mul(20.0));
    }

    private boolean areOnSameTeam(@NotNull Player player1, @NotNull Player player2) {
        return getTeamColor(player1) == getTeamColor(player2);
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
