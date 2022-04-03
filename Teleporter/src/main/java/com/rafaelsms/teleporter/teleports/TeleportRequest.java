package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.potocraft.util.AgeableTask;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import org.jetbrains.annotations.NotNull;

public class TeleportRequest extends AgeableTask {

    private final @NotNull User requester;
    private final @NotNull User teleporting;
    private final @NotNull User destination;

    public TeleportRequest(@NotNull TeleporterPlugin plugin,
                           @NotNull User requester,
                           @NotNull User teleporting,
                           @NotNull User destination) {
        super(plugin.getConfiguration().getTeleportRequestDurationTicks());
        this.requester = requester;
        this.teleporting = teleporting;
        this.destination = destination;
    }

    public @NotNull User getRequester() {
        return requester;
    }

    public @NotNull User getTeleporting() {
        return teleporting;
    }

    public @NotNull User getDestination() {
        return destination;
    }

    @Override
    public boolean shouldCancelTask() {
        return isUserUnavailable(teleporting) || isUserUnavailable(destination);
    }

    @Override
    public void onRestartDuration() {
    }

    @Override
    public void onTaskTick() {
    }

    @Override
    public void onTaskEnd() {
    }

    private boolean isUserUnavailable(@NotNull User user) {
        return false; // TODO user unavailable
    }
}
