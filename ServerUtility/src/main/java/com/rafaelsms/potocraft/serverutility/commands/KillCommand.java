package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class KillCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerUtilityPlugin plugin;

    public KillCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        return Util.convertList(plugin.getServer().getOnlinePlayers(), Player::getName);
    }
}
