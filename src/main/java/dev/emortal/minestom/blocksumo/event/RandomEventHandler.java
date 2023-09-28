package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.event.events.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class RandomEventHandler {

    private final @NotNull EventManager eventManager;
    private final @NotNull Instance instance;

    public RandomEventHandler(@NotNull BlockSumoGame game, @NotNull EventManager eventManager) {
        this.eventManager = eventManager;
        this.instance = game.getSpawningInstance();
    }

    public void startRandomEventTask() {
        this.instance.scheduler()
                .buildTask(this::startRandomEvent)
                .delay(TaskSchedule.minutes(2))
                .repeat(TaskSchedule.minutes(2))
                .schedule();
    }

    private void startRandomEvent() {
        BlockSumoEvent randomEvent = this.eventManager.findRandomEvent();

        this.setTimeToDusk();
        this.instance.scheduler().buildTask(this::resetTimeAdvance).delay(TaskSchedule.seconds(1)).schedule();

        this.instance.scheduler().buildTask(this::setTimeToNight).delay(TaskSchedule.seconds(10)).schedule();
        this.instance.scheduler()
                .buildTask(() -> {
                    this.resetTimeAdvance();
                    this.instance.setTime(8000);
                })
                .delay(TaskSchedule.seconds(11))
                .schedule();

        this.eventManager.startEvent(randomEvent);
    }

    private void setTimeToDusk() {
        this.instance.setTimeRate(400);
        this.instance.setTimeUpdate(Duration.ofMillis(50));
        this.instance.setTime(8000);
    }

    private void resetTimeAdvance() {
        this.instance.setTimeRate(0);
        this.instance.setTimeUpdate(null);
    }

    private void setTimeToNight() {
        this.instance.setTime(16000);
        this.instance.setTimeRate(80);
        this.instance.setTimeUpdate(Duration.ofMillis(50));
    }
}