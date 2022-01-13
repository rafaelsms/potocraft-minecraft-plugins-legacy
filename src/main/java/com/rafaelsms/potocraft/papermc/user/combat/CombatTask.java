package com.rafaelsms.potocraft.papermc.user.combat;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public class CombatTask implements Runnable {

    private final @NotNull PaperUser user;

    private final @NotNull Type type;
    private final @NotNull BossBar progressBar;
    private final long initialDelayTicks;

    private long remainingTicks;

    public CombatTask(@NotNull PaperPlugin plugin,
                      @NotNull PaperUser user,
                      @NotNull Type type,
                      long initialDelayTicks) {
        this.user = user;
        this.type = type;
        this.initialDelayTicks = initialDelayTicks;
        this.remainingTicks = initialDelayTicks;
        this.progressBar = BossBar.bossBar(plugin.getSettings().getCombatTitle(),
                                           BossBar.MAX_PROGRESS,
                                           BossBar.Color.RED,
                                           BossBar.Overlay.PROGRESS);
    }

    public @NotNull Type getType() {
        return type;
    }

    public void cancelTask() {
        user.getPlayer().hideBossBar(progressBar);
        user.setCombatTask(null);
    }

    public void reset() {
        // This will prevent the progress bar from blinking
        this.remainingTicks = initialDelayTicks;
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
            float progress = (remainingTicks * 1.0f) / initialDelayTicks;
            this.user.getPlayer().showBossBar(progressBar.progress(progress));
            return;
        }

        // Cancel task when remaining ticks ended
        cancelTask();
    }

    public enum Type {

        PLAYER,
        MOB;

        public boolean canOverride(@NotNull Type type) {
            // player combat has higher priority over mobs
            return this == PLAYER && type == MOB;
        }
    }
}
