package com.rafaelsms.potocraft.papermc.user.teleport;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface TeleportDestination {

    boolean isAvailable();

    @NotNull Location getLocation();

}
