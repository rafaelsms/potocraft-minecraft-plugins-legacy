package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Home;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeleteHomeCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerProfilePlugin plugin;

    public DeleteHomeCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return true;
        }
        if (!player.hasPermission(Permissions.TELEPORT_HOME)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        User user = plugin.getUserManager().getUser(player);
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration()
                                     .getTeleportHomeDeleteHelp(user.getProfile().getHomesSortedByDate()));
            return true;
        }

        String homeName = args[0];
        if (user.getProfile().removeHome(homeName)) {
            user.setTeleportTask(null);
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeDeleted());
            return true;
        }
        sender.sendMessage(plugin.getConfiguration().getTeleportHomeNotFound());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (!player.hasPermission(Permissions.TELEPORT_HOME)) {
            return List.of();
        }
        // Suggest only all homes
        return Util.convertList(plugin.getUserManager().getUser(player).getProfile().getHomesSortedByDate(),
                                Home::getName);
    }
}
