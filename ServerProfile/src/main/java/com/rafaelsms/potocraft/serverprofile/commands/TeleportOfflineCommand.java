package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class TeleportOfflineCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public TeleportOfflineCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player teleportingPlayer)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.TELEPORT_OFFLINE)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHelp());
            return true;
        }

        // Find the closest player by the name
        String playerName = args[0];
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayerIfCached(playerName);
        if (offlinePlayer == null) {
            sender.sendMessage(plugin.getConfiguration().getTeleportPlayerNotFound());
            return true;
        }
        Location location = offlinePlayer.getLocation();
        if (location == null) {
            sender.sendMessage(plugin.getConfiguration().getTeleportOfflineLocationNotFound());
            return true;
        }
        plugin.getUserManager()
              .getUser(teleportingPlayer)
              .teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }
}
