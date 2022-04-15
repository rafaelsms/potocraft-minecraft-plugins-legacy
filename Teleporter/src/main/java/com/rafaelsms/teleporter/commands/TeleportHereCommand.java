package com.rafaelsms.teleporter.commands;

import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TeleportHereCommand extends BaseTeleportCommand {

    public TeleportHereCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments) {
        if (arguments.length < 1) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportCommandHelp());
            return;
        }

        Optional<User> teleportingOptional = searchOnlinePlayer(arguments[0]);
        if (teleportingOptional.isEmpty()) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getCommandPlayerNotFound());
            return;
        }
        User teleporting = teleportingOptional.get();

        if (user.getPlayer().hasPermission(plugin.getPermissions().getBypassTeleportRequest())) {
            executeTeleportToUser(user, user, teleporting);
            return;
        }

        teleporting.addTeleportRequest(user, teleporting).getStatusMessage(plugin).ifPresent(user.getPlayer()::sendMessage);
    }
}
