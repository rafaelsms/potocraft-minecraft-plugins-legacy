package com.rafaelsms.teleporter.teleports;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface TeleportDestination {

    @NotNull Location getLocation();

    boolean isAvailable();

}
