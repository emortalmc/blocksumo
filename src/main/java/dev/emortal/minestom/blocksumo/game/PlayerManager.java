package dev.emortal.minestom.blocksumo.game;

import dev.emortal.minestom.blocksumo.map.BlockSumoInstance;
import dev.emortal.minestom.blocksumo.team.PlayerTeamManager;
import dev.emortal.minestom.blocksumo.utils.text.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public final class PlayerManager {

    private final BlockSumoGame game;
    private final PlayerDeathHandler deathHandler;
    private final PlayerRespawnHandler respawnHandler;
    private final PlayerTeamManager teamManager;

    private final Sidebar scoreboard;

    public PlayerManager(@NotNull BlockSumoGame game, int minAllowedHeight) {
        this.game = game;
        this.deathHandler = new PlayerDeathHandler(this, minAllowedHeight);
        this.respawnHandler = new PlayerRespawnHandler(game, this);
        this.teamManager = new PlayerTeamManager();
        this.scoreboard = new Sidebar(BlockSumoGame.TITLE);
    }

    public void registerPreGameListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            prepareInitialSpawn(player, player.getRespawnPoint());
            selectTeam(player);
            scoreboard.addViewer(player);
        });
    }

    private void prepareInitialSpawn(@NotNull Player player, @NotNull Pos pos) {
        respawnHandler.prepareSpawn(player, pos);

        final BlockSumoInstance instance = game.getInstance();
        final Entity entity = new Entity(EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloudMeta) entity.getEntityMeta()).setRadius(0);
        entity.setNoGravity(true);
        entity.setInstance(instance, pos).thenRun(() -> entity.addPassenger(player));
    }

    public void registerGameListeners(@NotNull EventNode<Event> eventNode) {
        deathHandler.registerDeathListener(eventNode);
    }

    public void addInitialTags(@NotNull Player player) {
        player.setTag(PlayerTags.LAST_DAMAGE_TIME, 0L);
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

    public @NotNull PlayerDeathHandler getDeathHandler() {
        return deathHandler;
    }

    public @NotNull PlayerRespawnHandler getRespawnHandler() {
        return respawnHandler;
    }

    public void broadcastMessage(@NotNull Component message) {
        game.getAudience().sendMessage(message);
    }
}
