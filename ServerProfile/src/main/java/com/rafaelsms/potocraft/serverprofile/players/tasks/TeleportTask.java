package com.rafaelsms.potocraft.serverprofile.players.tasks;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.TeleportDestination;
import com.rafaelsms.potocraft.serverprofile.players.User;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeleportTask implements Runnable {

    private final ServerProfilePlugin plugin;
    private final @NotNull User teleportingUser;
    private final @NotNull TeleportDestination destination;
    private final @Nullable User participantUser;
    private final @NotNull PlayerTeleportEvent.TeleportCause cause;
    private final long initialTaskTicks;

    private final BossBar progressBar;

    private long remainingTicks;

    public TeleportTask(@NotNull ServerProfilePlugin plugin,
                        @NotNull User teleportingUser,
                        @NotNull TeleportDestination destination,
                        @Nullable User participantUser,
                        PlayerTeleportEvent.@NotNull TeleportCause cause,
                        long initialTaskTicks) {
        this.plugin = plugin;
        this.teleportingUser = teleportingUser;
        this.progressBar = BossBar.bossBar(plugin.getConfiguration().getTeleportBarTitle(),
                                           BossBar.MIN_PROGRESS,
                                           BossBar.Color.YELLOW,
                                           BossBar.Overlay.PROGRESS);
        this.destination = destination;
        this.participantUser = participantUser;
        this.cause = cause;
        this.initialTaskTicks = initialTaskTicks;
        this.remainingTicks = initialTaskTicks;
    }

    private void sendMessage(@Nullable User user, @NotNull Component message) {
        if (user != null) {
            user.getPlayer().sendMessage(message);
        }
    }

    public void cancelTask() {
        teleportingUser.getPlayer().hideBossBar(progressBar);
        teleportingUser.setTeleportTask(null);
    }

    public void cancelTask(@NotNull Component message) {
        cancelTask();
        sendMessage(teleportingUser, message);
        sendMessage(participantUser, message);
    }

    private boolean isUserInvalid(@Nullable User user) {
        if (user == null) {
            return false;
        }
        if (user.getPlayer().isDead() || !user.getPlayer().isOnline()) {
            cancelTask(plugin.getConfiguration().getTeleportPlayerQuit());
            return true;
        }
        if (user.isInCombat()) {
            cancelTask(plugin.getConfiguration().getTeleportPlayerInCombat());
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        if (isUserInvalid(teleportingUser) || isUserInvalid(participantUser)) {
            // Is already cancelled above
            return;
        }
        if (participantUser != null && participantUser.isTeleporting()) {
            cancelTask(plugin.getConfiguration().getTeleportParticipantTeleporting());
            return;
        }

        if (destination.isUnavailable()) {
            cancelTask(plugin.getConfiguration().getTeleportDestinationUnavailable());
            return;
        }

        this.remainingTicks -= 1;
        if (this.remainingTicks > 0) {
            float progress = (initialTaskTicks - remainingTicks) * 1.0f / initialTaskTicks;
            Player player = teleportingUser.getPlayer();
            player.showBossBar(progressBar.progress(progress));
            player.getWorld()
                  .spawnParticle(Particle.PORTAL,
                                 player.getLocation().add(0, player.getHeight() * 0.7, 0),
                                 Math.round(progress * 32));
            return;
        }

        cancelTask();
        teleportingUser.teleportNow(destination.getLocation(), cause);
    }
}
