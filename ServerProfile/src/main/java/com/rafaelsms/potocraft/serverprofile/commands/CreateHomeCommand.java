package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateHomeCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public CreateHomeCommand(@NotNull ServerProfilePlugin plugin) {
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
        if (!player.hasPermission(Permissions.TELEPORT_HOME)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeCreateHelp());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);
        int numberOfHomes = user.getNumberOfHomes();
        if (user.getProfile().getHomes().size() >= numberOfHomes) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeMaxCapacity());
            return true;
        }

        String homeName = args[0];
        if (user.getProfile().addHome(homeName, player.getLocation())) {
            user.setTeleportTask(null);
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeCreated());
            return true;
        }
        sender.sendMessage(plugin.getConfiguration().getTeleportHomeAlreadyExists());
        return true;
    }
}
