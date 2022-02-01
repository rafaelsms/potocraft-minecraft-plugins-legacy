package com.rafaelsms.potocraft.blockprotection.util;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Optional;

public class Selection {

    private final @NotNull User user;
    private final @NotNull BossBar volumeUsageBar;

    private @Nullable SelectionBox selection;
    private @NotNull ZonedDateTime lastAction = ZonedDateTime.now();

    public Selection(@NotNull BlockProtectionPlugin plugin, @NotNull User user) {
        this.user = user;
        this.volumeUsageBar = BossBar.bossBar(Component.text("Volume usage"),
                                              BossBar.MIN_PROGRESS,
                                              BossBar.Color.YELLOW,
                                              BossBar.Overlay.PROGRESS);
        updateBar();
    }

    public @NotNull Optional<SelectionBox> getBox() {
        return Optional.ofNullable(selection);
    }

    public void setBox(@NotNull SelectionBox selectionBox) {
        this.selection = selectionBox;
        this.lastAction = ZonedDateTime.now();
        updateBar();
    }

    public float getVolumeUsageBar() {
        if (user.getProfile().getVolumeAvailable() <= 0.0) {
            return BossBar.MAX_PROGRESS;
        }
        if (selection == null) {
            return BossBar.MIN_PROGRESS;
        }
        return (float) Math.min(BossBar.MAX_PROGRESS, selection.getVolume() / user.getProfile().getVolumeAvailable());
    }

    public void showBar() {
        user.getPlayer().showBossBar(volumeUsageBar);
    }

    public void hideBar() {
        user.getPlayer().hideBossBar(volumeUsageBar);
    }

    private void updateBar() {
        float volumeUsage = getVolumeUsageBar();
        this.volumeUsageBar.progress(volumeUsage);
        if (volumeUsage >= BossBar.MAX_PROGRESS) {
            this.volumeUsageBar.color(BossBar.Color.RED);
            this.volumeUsageBar.name(Component.text("Exceeded volume usage! Can't expand area"));
        } else {
            this.volumeUsageBar.color(BossBar.Color.YELLOW);
            this.volumeUsageBar.name(Component.text("Volume usage"));
        }
        showBar();
    }
}
