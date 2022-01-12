package com.rafaelsms.potocraft.papermc.user.teleport;

import com.rafaelsms.potocraft.common.profile.Location;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class TeleportTask implements Runnable {

    private final @NotNull PaperPlugin plugin;
    private final @NotNull PaperUser user;

    private final @NotNull TeleportDestination teleportDestination;
    private final @NotNull PlayerTeleportEvent.TeleportCause cause;
    private final long initialDelayTicks;

    private final @NotNull BossBar progressBar;

    private long remainingTicks;

    public TeleportTask(@NotNull PaperPlugin plugin,
                        @NotNull PaperUser user,
                        @NotNull TeleportDestination teleportDestination,
                        PlayerTeleportEvent.@NotNull TeleportCause cause,
                        long initialDelayTicks) {
        this.plugin = plugin;
        this.user = user;
        this.teleportDestination = teleportDestination;
        this.cause = cause;
        this.initialDelayTicks = initialDelayTicks;
        this.remainingTicks = initialDelayTicks;
        this.progressBar = BossBar.bossBar(plugin.getSettings().getTeleportTitle(),
                                           BossBar.MIN_PROGRESS,
                                           BossBar.Color.YELLOW,
                                           BossBar.Overlay.PROGRESS);
    }

    public void cancelTask() {
        this.user.getPlayer().hideBossBar(progressBar);
        this.user.setTeleportTask(null);
    }

    @Override
    public void run() {
        Player player = this.user.getPlayer();
        // Check if user cannot teleport anymore
        if (!this.user.canTeleport()) {
            player.sendMessage(plugin.getSettings().getTeleportFailed());
            cancelTask();
            return;
        }
        // Check if destination is not available
        if (!teleportDestination.isAvailable()) {
            player.sendMessage(plugin.getSettings().getTeleportDestinationUnavailable());
            cancelTask();
            return;
        }

        // Decrease remaining ticks
        this.remainingTicks -= 1;
        if (this.remainingTicks > 0) {
            // Show progress bar
            float progress = (initialDelayTicks - remainingTicks) * 1.0f / initialDelayTicks;
            player.showBossBar(progressBar.progress(progress));
            return;
        }

        // Store back location and cancel teleport task
        org.bukkit.Location oldLocation = player.getLocation().clone();
        Location backLocation = Location.fromPlayer(plugin.getSettings().getServerName(), oldLocation);
        cancelTask();

        // If it is zero, let's teleport
        player.teleportAsync(teleportDestination.getLocation(), cause).whenComplete((success, throwable) -> {
            if (throwable != null || success == null || !success) {
                player.sendMessage(plugin.getSettings().getTeleportFailed());
                return;
            }
            player.sendMessage(plugin.getSettings().getTeleportedSuccessfully());
            user.getServerProfile().setBackLocation(backLocation);
        });
    }
}
