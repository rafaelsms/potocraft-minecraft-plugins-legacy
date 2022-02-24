package com.rafaelsms.potocraft.blockprotection.protection;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Selection implements Runnable {

    private final @NotNull BlockProtectionPlugin plugin;
    private final @NotNull User user;

    private final @NotNull BossBar volumeUsageBar;

    private final int timeToLive;
    private int age = 0;

    private @Nullable BoundingBox box = null;

    public Selection(@NotNull BlockProtectionPlugin plugin, @NotNull User user) {
        this.plugin = plugin;
        this.user = user;
        this.volumeUsageBar = volumeUsageBar;
        this.timeToLive = plugin.getConfiguration().getSelectionTimeToLive();
    }

    public void updateStatus() {
        if (box == null) {
            return;
        }
    }

    public void showBar() {
        // Assure status is defined on first being called
        updateStatus();
        user.getPlayer().showBossBar(volumeUsageBar);
    }

    public void hideBar() {
        user.getPlayer().hideBossBar(volumeUsageBar);
    }

    @Override
    public void run() {
        age++;
        if (age >= timeToLive) {
            user.setSelection(null);
            return;
        }
        updateStatus();
    }
}
