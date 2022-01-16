package com.rafaelsms.potocraft.serverprofile.players.tasks;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

public class CombatTask implements Runnable {

    private final @NotNull User user;

    private final BossBar progressBar;
    private final Type type;
    private final long initialTaskTicks;

    private long remainingTicks;

    public CombatTask(@NotNull ServerProfilePlugin plugin, @NotNull User user, Type type, long initialTaskTicks) {
        this.user = user;
        this.progressBar = BossBar.bossBar(plugin.getConfiguration().getCombatBarTitle(),
                                           BossBar.MAX_PROGRESS,
                                           BossBar.Color.RED,
                                           BossBar.Overlay.PROGRESS);
        this.type = type;
        this.initialTaskTicks = initialTaskTicks;
        this.remainingTicks = initialTaskTicks;
    }

    public Type getType() {
        return type;
    }

    public void cancelTask() {
        user.getPlayer().hideBossBar(progressBar);
        user.setCombatTask(null);
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

    public enum Type {

        MOB,
        PLAYER;

        public boolean canOverride(@NotNull Type other) {
            return this == PLAYER && other == MOB;
        }

        public boolean canResetTime(@NotNull Type other) {
            return this == other;
        }
    }
}
