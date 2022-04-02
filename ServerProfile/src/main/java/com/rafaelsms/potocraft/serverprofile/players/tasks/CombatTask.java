package com.rafaelsms.potocraft.serverprofile.players.tasks;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.serverprofile.util.CombatType;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public class CombatTask implements Runnable {

    private final @NotNull User user;

    private final BossBar progressBar;
    private final CombatType combatType;
    private final long initialTaskTicks;

    private long remainingTicks;

    public CombatTask(@NotNull ServerProfilePlugin plugin,
                      @NotNull User user,
                      CombatType combatType,
                      long initialTaskTicks) {
        this.user = user;
        this.progressBar = BossBar.bossBar(plugin.getConfiguration().getCombatBarTitle(),
                                           BossBar.MAX_PROGRESS,
                                           BossBar.Color.RED,
                                           BossBar.Overlay.PROGRESS);
        this.combatType = combatType;
        this.initialTaskTicks = initialTaskTicks;
        this.remainingTicks = initialTaskTicks;
    }

    public CombatType getType() {
        return combatType;
    }

    public void cancelTask() {
        user.getPlayer().hideBossBar(progressBar);
        user.clearCombatTask();
    }

    public void resetTime() {
        this.remainingTicks = initialTaskTicks;
    }

    @Override
    public void run() {
        if (user.getPlayer().isDead() || !user.getPlayer().isOnline()) {
            cancelTask();
            return;
        }

        remainingTicks -= 1;
        if (remainingTicks > 0) {
            float progress = (remainingTicks * 1.0f) / initialTaskTicks;
            user.getPlayer().showBossBar(progressBar.progress(progress));
            return;
        }

        cancelTask();
    }

}
