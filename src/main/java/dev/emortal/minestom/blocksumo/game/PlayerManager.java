package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.damage.PlayerDamageHandler;
import dev.emortal.minestom.blocksumo.damage.PlayerDeathHandler;
import dev.emortal.minestom.blocksumo.team.PlayerTeamManager;
import dev.emortal.minestom.blocksumo.utils.text.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public final class PlayerManager {

    private final BlockSumoGame game;
    private final PlayerDeathHandler deathHandler;
    private final PlayerRespawnHandler respawnHandler;
    private final PlayerTeamManager teamManager;
    private final PlayerDamageHandler damageHandler;

    private final Sidebar scoreboard;

    public PlayerManager(@NotNull BlockSumoGame game, int minAllowedHeight) {
        this.game = game;
        this.deathHandler = new PlayerDeathHandler(game, this, minAllowedHeight);
        this.respawnHandler = new PlayerRespawnHandler(game, this);
        this.teamManager = new PlayerTeamManager();
        this.damageHandler = new PlayerDamageHandler(game);
        this.scoreboard = new Sidebar(BlockSumoGame.TITLE);
    }

    public void registerPreGameListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            prepareInitialSpawn(player, player.getRespawnPoint());
            selectTeam(player);
            scoreboard.addViewer(player);
            updateLivesInHealth(player);
        });
    }

    public void updateLivesInHealth(@NotNull Player player) {
        final int lives = player.getTag(PlayerTags.LIVES);
        final float health = lives * 2;
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        player.setHealth(player.getMaxHealth());
    }

    private void prepareInitialSpawn(@NotNull Player player, @NotNull Pos pos) {
        respawnHandler.prepareSpawn(player, pos);

        final Instance instance = game.getInstance();
        final Entity entity = new Entity(EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloudMeta) entity.getEntityMeta()).setRadius(0);
        entity.setNoGravity(true);
        entity.setInstance(instance, pos).thenRun(() -> entity.addPassenger(player));
    }

    public void registerGameListeners(@NotNull EventNode<Event> eventNode) {
        deathHandler.registerListeners(eventNode);
        damageHandler.registerListeners(eventNode);
    }

    public void addInitialTags(@NotNull Player player) {
        player.setTag(PlayerTags.LAST_DAMAGE_TIME, 0L);
        player.setTag(PlayerTags.DEAD, false);
        player.setTag(PlayerTags.LIVES, (byte) 5);
    }

    public void setupWaitingScoreboard() {
        scoreboard.createLine(new Sidebar.ScoreboardLine("headerSpace", Component.empty(), 99));
        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "infoLine",
                Component.text().append(Component.text("Waiting for players...", NamedTextColor.GRAY)).build(),
                0
        ));
        scoreboard.createLine(new Sidebar.ScoreboardLine("footerSpacer", Component.empty(), -8));
        scoreboard.createLine(new Sidebar.ScoreboardLine(
                "ipLine",
                Component.text()
                        .append(Component.text(TextUtil.convertToSmallFont("mc.emortal.dev"), NamedTextColor.DARK_GRAY))
                        .append(Component.text("       ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
                        .build(),
                -9
        ));
    }

    private void selectTeam(@NotNull Player player) {
        teamManager.allocateTeam(player);
        scoreboard.createLine(new Sidebar.ScoreboardLine(player.getUuid().toString(), player.getDisplayName(), 5));
    }

    public void cleanUp() {
        teamManager.removeAllTeams();
        respawnHandler.stopAllScheduledRespawns();
        for (final Player player : game.getPlayers()) {
            cleanUpPlayer(player);
        }
    }

    private void cleanUpPlayer(@NotNull Player player) {
        player.removeTag(PlayerTags.TEAM_COLOR);
        player.removeTag(PlayerTags.LIVES);
        player.removeTag(PlayerTags.LAST_DAMAGE_TIME);
        player.removeTag(PlayerTags.DEAD);
        scoreboard.removeViewer(player);
    }

    public @NotNull PlayerDeathHandler getDeathHandler() {
        return deathHandler;
    }

    public @NotNull PlayerRespawnHandler getRespawnHandler() {
        return respawnHandler;
    }

    public @NotNull PlayerTeamManager getTeamManager() {
        return teamManager;
    }

    public @NotNull Sidebar getScoreboard() {
        return scoreboard;
    }

    public void broadcastMessage(@NotNull Component message) {
        game.getAudience().sendMessage(message);
    }
}
