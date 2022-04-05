package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.teleporter.player.User;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface TeleportDestination {

    @NotNull Location getLocation();

    boolean isAvailable();

    static @NotNull TeleportDestination ofPlayer(@NotNull User user) {
        return new PlayerDestination(user);
    }

    static @NotNull TeleportDestination ofLocation(@NotNull Location location) {
        return new LocationDestination(location);
    }
}
