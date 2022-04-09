package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.potocraft.util.TimerTask;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import net.kyori.adventure.audience.MessageType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TeleportRequest extends TimerTask<Void> {

    private final @NotNull TeleporterPlugin plugin;

    private final @NotNull User requested;
    private final @NotNull User requester;
    private final @NotNull User teleporting;

    public TeleportRequest(@NotNull TeleporterPlugin plugin,
                           @NotNull User requested,
                           @NotNull User requester,
                           @NotNull User teleporting) {
        super(plugin.getConfiguration().getTeleportRequestDurationTicks());
        this.plugin = plugin;
        this.requested = requested;
        this.requester = requester;
        this.teleporting = teleporting;
    }

    public @NotNull User getRequested() {
        return requested;
    }

    public @NotNull User getRequester() {
        return requester;
    }

    public @NotNull User getTeleporting() {
        return teleporting;
    }

    public @NotNull User getDestination() {
        if (Objects.equals(teleporting.getUniqueId(), requested.getUniqueId())) {
            // If requested is the one teleporting, the destination is the requester (teleport here)
            return requester;
        } else {
            // Otherwise, the one teleporting is the requester, so the destination is the requested (simple teleport)
            return requested;
        }
    }

    @Override
    public boolean shouldCancelTask() {
        return isUserUnavailable(getTeleporting()) || isUserUnavailable(getDestination());
    }

    @Override
    public void onRestartDuration() {
    }

    @Override
    public void onTaskStart() {
    }

    @Override
    public void onTaskTick() {
    }

    @Override
    protected Void executeTask() throws Exception {
        return null;
    }

    @Override
    public void taskCleanup() {
        requested.removeTeleportRequest(requester);
        requested.getPlayer()
                 .sendMessage(requester.getPlayer().identity(),
                              plugin.getConfiguration().getTeleportRequestExpired(requester.getPlayer().displayName()),
                              MessageType.CHAT);
    }

    private boolean isUserUnavailable(@NotNull User user) {
        return user.getTeleportStatus(false).isNegative();
    }
}
