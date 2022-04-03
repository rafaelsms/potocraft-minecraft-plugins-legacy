package com.rafaelsms.potocraft.util;

public abstract class AgeableTask implements TickableTask {

    private final int taskDurationTicks;
    private int remainingTicks;
    private boolean taskEnded = false;

    protected AgeableTask(int taskDurationTicks) {
        this.taskDurationTicks = taskDurationTicks;
        this.remainingTicks = taskDurationTicks;
    }

    @Override
    public void tick() {
        if (hasTaskEnded()) {
            return;
        }
        if (getRemainingTicks() <= 0 || shouldCancelTask()) {
            onTaskEnd();
            taskEnded = true;
            return;
        }
        remainingTicks--;
        onTaskTick();
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void restartDuration() {
        this.remainingTicks = taskDurationTicks;
        onRestartDuration();
    }

    public boolean hasTaskEnded() {
        return taskEnded;
    }

    public abstract boolean shouldCancelTask();

    public abstract void onRestartDuration();

    public abstract void onTaskTick();

    public abstract void onTaskEnd();
}
