package com.rafaelsms.teleporter.teleports;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationDestination implements TeleportDestination {

    private final @NotNull Location location;

    public LocationDestination(@NotNull Location location) {
        this.location = location;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public boolean isAvailable() {
        return location.isWorldLoaded();
    }
}
