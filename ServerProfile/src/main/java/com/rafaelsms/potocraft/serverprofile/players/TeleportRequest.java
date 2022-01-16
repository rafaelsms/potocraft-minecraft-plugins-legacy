package com.rafaelsms.potocraft.serverprofile.players;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TeleportRequest {

    private final @NotNull User requester;
    private final @NotNull User teleporting;
    private final @NotNull User destination;

    private @NotNull ZonedDateTime askingTime = ZonedDateTime.now();
    private boolean cancelled = false;

    public TeleportRequest(@NotNull User requester, @NotNull User teleporting, @NotNull User destination) {
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

    public void updateAskingTime() {
        this.askingTime = ZonedDateTime.now();
    }

    public boolean isExpired(@NotNull Duration requestDuration) {
        if (cancelled) {
            return true;
        }
        if (isUserOffline(destination) || isUserOffline(teleporting)) {
            return true;
        }
        return askingTime.isBefore(ZonedDateTime.now().minus(requestDuration));
    }

    private boolean isUserOffline(@NotNull User user) {
        return user.getPlayer().isDead() || !user.getPlayer().isOnline();
    }

    public void cancel() {
        this.cancelled = true;
    }
}
