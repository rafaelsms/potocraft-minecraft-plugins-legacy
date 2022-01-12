package com.rafaelsms.potocraft.papermc.user.teleport;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TeleportRequest {

    private final @NotNull PaperPlugin plugin;

    private final @NotNull PaperUser requester;
    private final @NotNull PaperUser teleportingUser;
    private final @NotNull PaperUser destinationUser;

    private @NotNull ZonedDateTime askingTime = ZonedDateTime.now();
    private boolean cancelled = false;

    public TeleportRequest(@NotNull PaperPlugin plugin,
                           @NotNull PaperUser requester,
                           @NotNull PaperUser teleportingUser,
                           @NotNull PaperUser destinationUser) {
        this.plugin = plugin;
        this.requester = requester;
        this.teleportingUser = teleportingUser;
        this.destinationUser = destinationUser;
    }

    public @NotNull PaperUser getRequester() {
        return requester;
    }

    public @NotNull PaperUser getTeleportingUser() {
        return teleportingUser;
    }

    public @NotNull PaperUser getDestinationUser() {
        return destinationUser;
    }

    public void updateAskingTime() {
        this.askingTime = ZonedDateTime.now();
    }

    public boolean isExpired() {
        if (cancelled) {
            return true;
        }
        if (!destinationUser.getPlayer().isOnline() || !teleportingUser.getPlayer().isOnline()) {
            return true;
        }
        Duration requestDuration = plugin.getSettings().getTeleportRequestDuration();
        return ZonedDateTime.now().minus(requestDuration).isAfter(askingTime);
    }

    public TeleportTask.Builder accept(@NotNull PaperPlugin plugin, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        TeleportTask.Builder builder = TeleportTask.Builder
                .builder(plugin, teleportingUser, destinationUser, cause)
                .withParticipant(requester);
        cancelled = true;
        return builder;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
