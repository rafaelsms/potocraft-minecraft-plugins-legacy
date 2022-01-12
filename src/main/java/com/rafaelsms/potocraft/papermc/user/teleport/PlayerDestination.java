package com.rafaelsms.potocraft.papermc.user.teleport;

import com.rafaelsms.potocraft.papermc.user.PaperUser;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class PlayerDestination implements TeleportDestination {

    private final @NotNull PaperUser destination;

    public PlayerDestination(@NotNull PaperUser destination) {
        this.destination = destination;
    }

    @Override
    public boolean isAvailable() {
        return !destination.getPlayer().isDead() && destination.getPlayer().isOnline();
    }

    @Override
    public @NotNull Location getLocation() {
        return destination.getPlayer().getLocation();
    }
}
