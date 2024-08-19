package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.team.TeamColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public final class PlayerTags {
    public static final @NotNull Tag<Long> LAST_DAMAGE_TIME = Tag.Long("last_damage_timestamp");
    public static final @NotNull Tag<TeamColor> TEAM_COLOR = Tag.String("team_color").map((str) -> new TeamColor(TextColor.fromHexString(str)), (team) -> team.getTextColor().asHexString());
    public static final @NotNull Tag<Byte> LIVES = Tag.Byte("lives");
    public static final @NotNull Tag<Integer> KILLS = Tag.Integer("kills");
    public static final @NotNull Tag<Integer> FINAL_KILLS = Tag.Integer("final_kills");
    public static final @NotNull Tag<Boolean> CAN_BE_HIT = Tag.Boolean("can_be_hit");
    public static final @NotNull Tag<Long> SPAWN_PROTECTION_TIME = Tag.Long("spawn_protection_time");

    private PlayerTags() {
    }
}
