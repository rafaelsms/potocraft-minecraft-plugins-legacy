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

public class FlightCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerUtilityPlugin plugin;

    public FlightCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_FLY)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        boolean otherPlayer = false;
        Player flyingPlayer;
        if (args.length > 0) {
            // Check if player can fly other players
            if (!sender.hasPermission(Permissions.COMMAND_FLY_OTHERS)) {
                sender.sendMessage(plugin.getServer().getPermissionMessage());
                return true;
            }
            Optional<? extends Player> optionalPlayer =
                    TextUtil.closestMatch(plugin.getServer().getOnlinePlayers(), Player::getName, args[0]);
            if (optionalPlayer.isPresent()) {
                flyingPlayer = optionalPlayer.get();
                otherPlayer = true;
            } else {
                return true;
            }
        } else if (sender instanceof Player player) {
            flyingPlayer = player;
        } else {
            sender.sendMessage(plugin.getConfiguration().getFlyHelp());
            return true;
        }

        flyingPlayer.setAllowFlight(!flyingPlayer.getAllowFlight());
        if (otherPlayer) {
            sender.sendMessage(plugin.getConfiguration()
                                     .getFlyStatus(flyingPlayer.getName(), flyingPlayer.getAllowFlight()));
        }
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
