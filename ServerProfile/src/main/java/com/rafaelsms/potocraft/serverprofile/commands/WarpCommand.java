package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class WarpCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public WarpCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return true;
        }
        if (!player.hasPermission(Permissions.TELEPORT_WARP)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        Optional<List<Warp>> optionalList = plugin.getDatabase().getWarps();
        if (optionalList.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpFailedToRetrieve());
            return true;
        }
        List<Warp> warps = optionalList.get();
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpList(warps));
            return true;
        }

        String warpName = args[0];
        for (Warp warp : warps) {
            if (warp.getName().equalsIgnoreCase(warpName)) {
                User user = plugin.getUserManager().getUser(player);
                if (user.isPlayerTeleportBlocked(true)) {
                    return true;
                }
                Optional<Location> optionalLocation = warp.getLocation(plugin);
                if (optionalLocation.isEmpty()) {
                    player.sendMessage(plugin.getConfiguration().getTeleportDestinationUnavailable());
                    return true;
                }
                user.teleport(optionalLocation.get(), PlayerTeleportEvent.TeleportCause.COMMAND);
                return true;
            }
        }

        sender.sendMessage(plugin.getConfiguration().getTeleportWarpNotFound());
        return true;
    }
}
