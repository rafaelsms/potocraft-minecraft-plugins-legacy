package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateWarpCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public CreateWarpCommand(@NotNull ServerProfilePlugin plugin) {
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
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpManageHelp());
            return true;
        }

        String warpName = args[0];
        try {
            plugin.getDatabase().replaceWarp(new Warp(warpName, player.getLocation()));
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpManageSuccess());
        } catch (Database.DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWarpManageDatabaseFailure());
        }
        return true;
    }
}
