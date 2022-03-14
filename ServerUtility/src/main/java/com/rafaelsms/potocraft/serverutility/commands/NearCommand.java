package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class NearCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerUtilityPlugin plugin;

    public NearCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_NEAR)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfiguration().getPlayerNotFound());
            } else {
                showNearbyPlayers(sender, (Player) sender);
            }
            return true;
        }
        if (!sender.hasPermission(Permissions.COMMAND_NEAR_OTHERS)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        Optional<? extends Player> optionalPlayer =
                TextUtil.closestMatch(plugin.getServer().getOnlinePlayers(), Player::getName, args[0]);
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getPlayerNotFound());
            return true;
        }
        showNearbyPlayers(sender, optionalPlayer.get());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        return Util.convertList(plugin.getServer().getOnlinePlayers(), Player::getName);
    }

    private void showNearbyPlayers(@NotNull CommandSender sender, @NotNull Player player) {
        // Calculate player distances
        HashMap<Player, Double> distanceMap = new HashMap<>();
        double maxDistance = plugin.getConfiguration().getNearbyPlayersRange();
        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            if (otherPlayer.getGameMode() == GameMode.CREATIVE || otherPlayer.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (otherPlayer.hasPermission(Permissions.COMMAND_NEAR_BYPASS)) {
                continue;
            }
            double distance = player.getLocation().distance(otherPlayer.getLocation());
            if (distance <= maxDistance) {
                distanceMap.put(otherPlayer, distance);
            }
        }
        // Show sender
        sender.sendMessage(plugin.getConfiguration().getNearbyPlayers(distanceMap));
    }
}
