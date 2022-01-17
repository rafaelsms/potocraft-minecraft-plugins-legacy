package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TeleportHereCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public TeleportHereCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player destinationPlayer)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.TELEPORT) || !sender.hasPermission(Permissions.TELEPORT_HERE)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHelp());
            return true;
        }

        // Find closest player by the name
        String playerName = args[0];
        Optional<? extends Player> optionalPlayer =
                TextUtil.closestStringMatch(plugin.getServer().getOnlinePlayers(), Player::getName, playerName);
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getTeleportPlayerNotFound());
            return true;
        }
        Player teleportingPlayer = optionalPlayer.get();
        if (teleportingPlayer.getUniqueId().equals(destinationPlayer.getUniqueId())) {
            sender.sendMessage(plugin.getConfiguration().getTeleportPlayerNotFound());
            return true;
        }

        // Teleport to player or request
        User teleportingUser = plugin.getUserManager().getUser(teleportingPlayer);
        if (sender.hasPermission(Permissions.TELEPORT_NO_REQUEST)) {
            teleportingUser.teleportNow(destinationPlayer.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }

        User destinationUser = plugin.getUserManager().getUser(destinationPlayer);
        if (teleportingUser.addTeleportRequest(destinationUser, false)) {
            teleportingPlayer.sendMessage(plugin
                                                  .getConfiguration()
                                                  .getTeleportHereRequestReceived(destinationPlayer.getName()));
        }
        sender.sendMessage(plugin.getConfiguration().getTeleportRequestSent(teleportingPlayer.getName()));
        return true;
    }
}
