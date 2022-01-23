package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VanishCommand implements CommandExecutor {

    private final @NotNull ServerUtilityPlugin plugin;

    public VanishCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.COMMAND_VANISH)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        // Toggle vanish
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getVanishManager().toggleVanish(player);
            if (plugin.getVanishManager().isVanished(player)) {
                sender.sendMessage(plugin.getConfiguration().getPlayerVanished());
            } else {
                sender.sendMessage(plugin.getConfiguration().getPlayerAppeared());
            }
        });
        return true;
    }
}
