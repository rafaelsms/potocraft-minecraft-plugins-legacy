package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.potocraft.util.TimerTask;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public class TeleportingTask extends TimerTask<TeleportResult> {

    private final @NotNull User teleporting;
    private final @NotNull TeleportDestination teleportDestination;

    private final @NotNull BossBar progressBar;

    public TeleportingTask(@NotNull TeleporterPlugin plugin,
                           @NotNull User teleporting,
                           @NotNull TeleportDestination teleportDestination,
                           int teleportDurationTicks) {
        super(teleportDurationTicks);
        this.teleporting = teleporting;
        this.teleportDestination = teleportDestination;
        this.progressBar = BossBar.bossBar(plugin.getConfiguration().getProgressBarTeleporting(),
                                           BossBar.MAX_PROGRESS,
                                           BossBar.Color.YELLOW,
                                           BossBar.Overlay.PROGRESS);
    }

    @Override
    public boolean shouldCancelTask() {
        return !teleportDestination.isAvailable() || teleporting.canTeleport(false).isFailed();
    }

    @Override
    public void onRestartDuration() {
    }

    @Override
    public void onTaskStart() {
        teleporting.getPlayer().showBossBar(progressBar);
    }

    @Override
    public void onTaskTick() {
        int initialTicks = getInitialTicks();
        if (initialTicks == 0) {
            return;
        }
        int remainingTicks = getRemainingTicks();
        float progress = (float) (initialTicks - Math.min(initialTicks, remainingTicks)) / initialTicks;
        progressBar.progress(progress);
    }

    @Override
    protected TeleportResult executeTask() throws Exception {
        return teleporting.teleportNow(teleportDestination).get();
    }

    @Override
    public void taskCleanup() {
        teleporting.getPlayer().hideBossBar(progressBar);
        teleporting.clearTeleportingTask();
    }
}
