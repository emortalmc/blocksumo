package dev.emortal.minestom.blocksumo.event.events;

import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public final class SmallBorderEvent implements BlockSumoEvent {
    private static final Component START_MESSAGE = Component.text()
            .append(Component.text("Uh oh...", NamedTextColor.RED))
            .append(Component.text(" the border is shrinking!", NamedTextColor.YELLOW))
            .build();
    private final @NotNull BlockSumoGame game;
    private long startShrink = 0;

    public SmallBorderEvent(@NotNull BlockSumoGame game) {
        this.game = game;
    }

    @Override
    public void start() {
        Instance instance = this.game.getInstance();

        startShrink = System.currentTimeMillis();
        this.game.setRespawnRadius(5);
        instance.getWorldBorder().setDiameter(40, 0);
        instance.getWorldBorder().setDiameter(12, 15000);

        var task = instance.scheduler().buildTask(() -> {
            for (Player player : this.game.getPlayers()) {
                if (player.getGameMode() != GameMode.SURVIVAL) return;

                if (outOfBorder(player)) {
                    this.game.getPlayerManager().getDeathHandler().kill(player, null);
                }
            }
        }).repeat(TaskSchedule.tick(1)).schedule();

        instance.scheduler().buildTask(() -> {
            task.cancel();
            this.game.setRespawnRadius(14);
            instance.getWorldBorder().setDiameter(40000, 1);
        }).delay(TaskSchedule.tick(20 * 60)).schedule();
    }

    @Override
    public @NotNull Component getStartMessage() {
        return START_MESSAGE;
    }

    public boolean outOfBorder(Player player) {
        Pos p = player.getPosition();
        double borderSize = (getBorderSize() / 2) + 1.5;

        if (p.x() > borderSize || p.x() < -borderSize || p.z() > borderSize || p.z() < -borderSize) return true;
        return false;
    }
    public double getBorderSize() {
        long millisPassed = System.currentTimeMillis() - startShrink;
        double percent = (double) millisPassed / 15000.0;
        return Math.max(14, lerp(40, 12, percent));
    }

    public double lerp(double a, double b, double f) {
        return a * (1.0 - f) + (b * f);
    }

    public boolean isPlayerInBorder(Player player) {
        Pos position = player.getPosition();
        return !(position.x() > 6) && !(position.x() < 6) && !(position.z() > 6) && !(position.z() < 6);
    }

}
