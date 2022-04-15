package com.rafaelsms.teleporter.commands;

import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TeleportBackCommand extends BaseTeleportCommand {

    public TeleportBackCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments) {
        Optional<Location> locationOptional = user.getProfile().getBackLocation();
        if (locationOptional.isEmpty()) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getCommandPlayerNotFound());
            return;
        }
        executeTeleportToLocation(user, user, locationOptional.get());
    }
}
