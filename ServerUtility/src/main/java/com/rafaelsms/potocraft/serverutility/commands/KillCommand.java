package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KillCommand implements CommandExecutor {

    private final @NotNull ServerUtilityPlugin plugin;

    public KillCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_KILL)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getKillHelp());
            return true;
        }

        // Search player
        Optional<? extends Player> optionalPlayer =
                TextUtil.closestMatch(plugin.getServer().getOnlinePlayers(), Player::getName, args[0]);
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getPlayerNotFound());
            return true;
        }

        // Kill player
        Player player = optionalPlayer.get();
        player.setKiller(player);
        // If sender is a player, set as the killer
        if (sender instanceof Player killer) {
            player.setKiller(killer);
        }
        player.setHealth(0.0);
        return true;
    }
}
