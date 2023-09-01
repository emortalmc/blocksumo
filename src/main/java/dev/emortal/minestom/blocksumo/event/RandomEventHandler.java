package dev.emortal.minestom.blocksumo.event;

import dev.emortal.minestom.blocksumo.event.events.BlockSumoEvent;
import dev.emortal.minestom.blocksumo.game.BlockSumoGame;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class RandomEventHandler {

    private final BlockSumoGame game;
    private final EventManager eventManager;

    public RandomEventHandler(@NotNull BlockSumoGame game, @NotNull EventManager eventManager) {
        this.game = game;
        this.eventManager = eventManager;
    }

    public void startRandomEventTask() {
        game.getSpawningInstance().scheduler()
                .buildTask(this::startRandomEvent)
                .delay(TaskSchedule.minutes(2))
                .repeat(TaskSchedule.minutes(2))
                .schedule();
    }

    private void startRandomEvent() {
        final Instance instance = game.getSpawningInstance();
        final BlockSumoEvent randomEvent = eventManager.findRandomEvent();

        setTimeToDusk();
        instance.scheduler().buildTask(this::resetTimeAdvance).delay(TaskSchedule.seconds(1)).schedule();

        instance.scheduler().buildTask(this::setTimeToNight).delay(TaskSchedule.seconds(10)).schedule();
        instance.scheduler().buildTask(() -> {
            resetTimeAdvance();
            instance.setTime(8000);
        }).delay(TaskSchedule.seconds(11)).schedule();

        eventManager.startEvent(randomEvent);
    }

    private void setTimeToDusk() {
        final Instance instance = game.getSpawningInstance();
        instance.setTimeRate(400);
        instance.setTimeUpdate(Duration.ofMillis(50));
        instance.setTime(8000);
    }

    private void resetTimeAdvance() {
        final Instance instance = game.getSpawningInstance();
        instance.setTimeRate(0);
        instance.setTimeUpdate(null);
    }

    private void setTimeToNight() {
        final Instance instance = game.getSpawningInstance();
        instance.setTime(16000);
        instance.setTimeRate(80);
        instance.setTimeUpdate(Duration.ofMillis(50));
    }
}
