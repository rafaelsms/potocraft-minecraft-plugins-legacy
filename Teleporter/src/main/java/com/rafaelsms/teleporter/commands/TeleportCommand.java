package com.rafaelsms.teleporter.commands;

import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TeleportCommand extends BaseTeleportCommand {

    public TeleportCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments) {
        if (arguments.length < 1) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportCommandHelp());
            return;
        }

        Optional<User> destinationOptional = searchOnlinePlayer(arguments[0]);
        if (destinationOptional.isEmpty()) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getCommandPlayerNotFound());
            return;
        }
        User destination = destinationOptional.get();

        if (user.getPlayer().hasPermission(plugin.getPermissions().getBypassTeleportRequest())) {
            executeTeleportToUser(user, user, destination);
            return;
        }

        destination.addTeleportRequest(user, user).getStatusMessage(plugin).ifPresent(user.getPlayer()::sendMessage);
    }
}
