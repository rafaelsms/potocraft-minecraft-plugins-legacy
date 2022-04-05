package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.teleporter.player.User;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class PlayerDestination implements TeleportDestination {

    private final @NotNull User destination;

    public PlayerDestination(@NotNull User destination) {
        this.destination = destination;
    }

    @Override
    public @NotNull Location getLocation() {
        return destination.getPlayer().getLocation();
    }

    @Override
    public boolean isAvailable() {
        return !destination.canTeleport(false).isFailed();
    }
}
