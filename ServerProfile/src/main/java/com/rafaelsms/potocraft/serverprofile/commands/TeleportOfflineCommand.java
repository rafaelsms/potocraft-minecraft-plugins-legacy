package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.util.CommandUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

        // Find the offline player by the name
        String playerName = args[0];
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayerIfCached(playerName);
        if (offlinePlayer == null) {
            // Search in the database if player is not found in the cache
            Optional<Profile> profileOptional = CommandUtil.handlePlayerSearch(plugin, sender, playerName);
            if (profileOptional.isEmpty()) {
                return true;
            }
            Profile profile = profileOptional.get();
            offlinePlayer = plugin.getServer().getOfflinePlayer(profile.getPlayerId());
        }

        // Get offline player's location
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
