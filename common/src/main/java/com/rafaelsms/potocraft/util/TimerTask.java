package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class TimerTask<T> implements TickableTask {

    private final @NotNull CompletableFuture<T> future;

    private final int taskTimerDurationTicks;
    private int remainingTicks;

    private boolean taskCancelled = false;
    private boolean taskEnded = false;
    private boolean firstTick = true;

    protected TimerTask(int taskTimerDurationTicks) {
        this.taskTimerDurationTicks = taskTimerDurationTicks;
        this.remainingTicks = taskTimerDurationTicks;
        this.future = new CompletableFuture<>();
    }

    @Override
    public void tick() {
        // If already ended, ignore (we should stop ticking it elsewhere)
        if (hasTaskEnded()) {
            return;
        }

        // If we should cancel the task, end it early completing it exceptionally
        if (shouldCancelTask() || remainingTicks <= 0 || taskCancelled) {
            endTask();
            return;
        }

        // Always tick
        remainingTicks--;
        if (firstTick) {
            onTaskStart();
            firstTick = false;
        }
        onTaskTick();

        // If timer ended, execute task and clean up
        if (remainingTicks <= 0) {
            try {
                future.complete(executeTask());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
            endTask();
        }
    }

    private void endTask() {
        this.remainingTicks = 0;
        taskEnded = true;
        taskCleanup();
        if (!future.isDone()) {
            future.completeExceptionally(new IllegalStateException("Task wasn't completed"));
        }
    }

    public void cancelTask() {
        this.taskCancelled = true;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public int getInitialTicks() {
        return taskTimerDurationTicks;
    }

    public void restartDuration() {
        this.remainingTicks = taskTimerDurationTicks;
        firstTick = true;
        taskEnded = false;
        onRestartDuration();
    }

    public boolean hasTaskEnded() {
        return taskEnded;
    }

    public @NotNull CompletableFuture<T> getFuture() {
        return future;
    }

    public abstract boolean shouldCancelTask();

    public abstract void onRestartDuration();

    public abstract void onTaskStart();

    public abstract void onTaskTick();

    protected abstract T executeTask() throws Exception;

    public abstract void taskCleanup();
}
