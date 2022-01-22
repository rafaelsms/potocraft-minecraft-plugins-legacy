package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Home;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public HomeCommand(@NotNull ServerProfilePlugin plugin) {
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

        User user = plugin.getUserManager().getUser(player);
        int maxHomesSize = user.getMaxHomesSize();
        int homesSize = user.getProfile().getHomesSize();
        List<Home> homesSorted = user.getProfile().getHomesSortedByDate();

        if (homesSize == 0) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeHelp());
            return true;
        } else if (homesSize > 1) {
            if (args.length != 1) {
                sender.sendMessage(plugin.getConfiguration().getTeleportHomeList(homesSorted));
            } else {
                String homeName = args[0];
                for (int i = 0; i < Math.min(maxHomesSize, homesSorted.size()); i++) {
                    Home home = homesSorted.get(i);
                    if (home.getName().equalsIgnoreCase(homeName)) {
                        if (user.isPlayerTeleportBlocked(true)) {
                            return true;
                        }
                        user.teleport(home.toLocation(plugin), PlayerTeleportEvent.TeleportCause.COMMAND);
                        return true;
                    }
                }
            }
        } else if (maxHomesSize >= 1) {
            if (user.isPlayerTeleportBlocked(true)) {
                return true;
            }
            Home home = homesSorted.get(0);
            user.teleport(home.toLocation(plugin), PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }

        sender.sendMessage(plugin.getConfiguration().getTeleportHomeNotFound());
        return true;
    }
}
