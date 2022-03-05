package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Home;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.serverprofile.util.CommandUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final Pattern playerHomeExtractor = Pattern.compile("(\\S+):(\\S+)?", Pattern.CASE_INSENSITIVE);
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
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);
        if (args.length == 1 && player.hasPermission(Permissions.TELEPORT_HOME_OTHERS)) {
            Matcher matcher = playerHomeExtractor.matcher(args[0]);
            if (matcher.matches()) {
                String playerName = matcher.group(1);
                String homeName = matcher.group(2);

                // Search by player name
                Optional<Profile> profileOptional = CommandUtil.handlePlayerSearch(plugin, sender, playerName);
                if (profileOptional.isEmpty()) {
                    return true;
                }
                Profile profile = profileOptional.get();
                if (homeName == null || homeName.isEmpty()) {
                    // Send list of homes
                    player.sendMessage(plugin.getConfiguration().getTeleportHomeList(profile.getHomesSortedByDate()));
                } else {
                    Optional<Home> home = profile.getHome(homeName);
                    if (home.isEmpty()) {
                        // Warn that no home was found
                        player.sendMessage(plugin.getConfiguration().getTeleportHomeNotFound());
                        return true;
                    }
                    // Teleport player to home
                    Optional<Location> locationOptional = home.get().toLocation(plugin);
                    if (locationOptional.isEmpty()) {
                        player.sendMessage(plugin.getConfiguration().getTeleportDestinationUnavailable());
                        return true;
                    }
                    user.teleport(locationOptional.get(), PlayerTeleportEvent.TeleportCause.COMMAND);
                }
                return true;
            }
        }

        int maxHomesSize = user.getMaxHomesSize();
        int homesSize = user.getProfile().getHomesSize();
        List<Home> homesSorted = user.getProfile().getHomesSortedByDate();

        if (homesSize == 0) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeHelp());
            return true;
        } else if (homesSize > 1) {
            if (args.length != 1) {
                sender.sendMessage(plugin.getConfiguration().getTeleportHomeList(homesSorted));
                return true;
            } else {
                String homeName = args[0];
                for (int i = 0; i < Math.min(maxHomesSize, homesSorted.size()); i++) {
                    Home home = homesSorted.get(i);
                    if (home.getName().equalsIgnoreCase(homeName)) {
                        if (user.isPlayerTeleportBlocked(true)) {
                            return true;
                        }
                        Optional<Location> locationOptional = home.toLocation(plugin);
                        if (locationOptional.isEmpty()) {
                            player.sendMessage(plugin.getConfiguration().getTeleportDestinationUnavailable());
                            return true;
                        }
                        user.teleport(locationOptional.get(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        return true;
                    }
                }
            }
        } else if (maxHomesSize >= 1) {
            if (user.isPlayerTeleportBlocked(true)) {
                return true;
            }
            Home home = homesSorted.get(0);
            Optional<Location> locationOptional = home.toLocation(plugin);
            if (locationOptional.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getTeleportDestinationUnavailable());
                return true;
            }
            user.teleport(locationOptional.get(), PlayerTeleportEvent.TeleportCause.COMMAND);
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
        // Check if player has no homes
        User user = plugin.getUserManager().getUser(player);
        int homesSize = user.getProfile().getHomesSize();
        if (homesSize == 0) {
            return List.of();
        }

        // Suggest only available homes
        int maxHomesSize = user.getMaxHomesSize();
        List<Home> homesSorted = user.getProfile().getHomesSortedByDate();
        return Util.convertList(homesSorted.subList(0, Math.min(homesSize, maxHomesSize) - 1), Home::getName);
    }
}
