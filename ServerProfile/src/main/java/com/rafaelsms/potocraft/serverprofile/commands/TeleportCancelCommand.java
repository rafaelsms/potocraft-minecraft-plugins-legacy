package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.TeleportRequest;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeleportCancelCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public TeleportCancelCommand(@NotNull ServerProfilePlugin plugin) {
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
        if (!sender.hasPermission(Permissions.TELEPORT)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        for (User user : plugin.getUserManager().getUsers()) {
            for (TeleportRequest teleportRequest : user.getTeleportRequests()) {
                if (teleportRequest.getRequester().getPlayerId().equals(player.getUniqueId())) {
                    teleportRequest.cancel();
                }
            }
        }
        player.sendMessage(plugin.getConfiguration().getTeleportRequestsSentCancelled());
        return true;
    }
}
