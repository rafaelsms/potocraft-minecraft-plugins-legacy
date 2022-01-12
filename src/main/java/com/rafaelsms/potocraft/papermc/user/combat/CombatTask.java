package com.rafaelsms.potocraft.papermc.user.combat;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public class CombatTask implements Runnable {

    private final @NotNull PaperUser user;

    private final @NotNull BossBar progressBar;
    private final long initialDelayTicks;

    private long remainingTicks;

    public CombatTask(@NotNull PaperPlugin plugin, @NotNull PaperUser user, long initialDelayTicks) {
        this.user = user;
        this.initialDelayTicks = initialDelayTicks;
        this.remainingTicks = initialDelayTicks;
        this.progressBar = BossBar.bossBar(plugin.getSettings().getCombatTitle(),
                                           BossBar.MIN_PROGRESS,
                                           BossBar.Color.YELLOW,
                                           BossBar.Overlay.PROGRESS);
    }

    public void cancelTask() {
        user.getPlayer().hideBossBar(progressBar);
        user.setCombatTask(null);
    }

    @Override
    public void run() {
        // Check if player is online and alive
        if (!user.getPlayer().isOnline() || user.getPlayer().isDead()) {
            cancelTask();
            return;
        }

        // Decrease remaining ticks
        this.remainingTicks -= 1;
        if (this.remainingTicks > 0) {
            // Show progress bar
            float progress = (initialDelayTicks - remainingTicks) * 1.0f / initialDelayTicks;
            this.user.getPlayer().showBossBar(progressBar.progress(progress));
            return;
        }

        // Cancel task when remaining ticks ended
        cancelTask();
    }
}
