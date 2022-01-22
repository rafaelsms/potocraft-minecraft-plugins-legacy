package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeleteWarpCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public DeleteWarpCommand(@NotNull ServerProfilePlugin plugin) {
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
        if (!player.hasPermission(Permissions.TELEPORT_WARP_MANAGE)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpManageHelp());
            return true;
        }

        try {
            String warpRegex = args[0];
            plugin.getDatabase().deleteWarp(warpRegex);
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpManageSuccess());
        } catch (Database.DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpManageDatabaseFailure());
        }
        return true;
    }
}
