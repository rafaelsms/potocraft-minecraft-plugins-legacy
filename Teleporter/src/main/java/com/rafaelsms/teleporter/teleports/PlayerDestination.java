package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.teleporter.player.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerDestination implements TeleportDestination {

    private final @NotNull User destiny;

    public PlayerDestination(@NotNull User destiny) {
        this.destiny = destiny;
    }

    @Override
    public @NotNull Location getLocation() {
        return destiny.getPlayer().getLocation();
    }

    @Override
    public boolean isAvailable() {
        Player player = destiny.getPlayer();
        return player.isOnline() && !player.isDead();
    }
}
