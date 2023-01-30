package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.team.TeamColor;
import net.minestom.server.tag.Tag;

public final class PlayerTags {
    public static final Tag<Long> LAST_DAMAGE_TIME = Tag.Long("last_damage_timestamp");
    public static final Tag<TeamColor> TEAM_COLOR = Tag.String("team_color").map(TeamColor::valueOf, TeamColor::name);
    public static final Tag<Boolean> DEAD = Tag.Boolean("dead");
    public static final Tag<Byte> LIVES = Tag.Byte("lives");
    public static final Tag<Boolean> CAN_BE_HIT = Tag.Boolean("can_be_hit");
    public static final Tag<Long> SPAWN_PROTECTION_TIME = Tag.Long("spawn_protection_time");

    private PlayerTags() {
    }
}
