package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class CreateHomeCommand implements CommandExecutor, TabCompleter {

    private final Pattern homeNamePattern = Pattern.compile("^[a-z0-9_-]{1,16}$", Pattern.CASE_INSENSITIVE);
    private final Map<UUID, HomeReplacement> replaceHomeAttempt = Collections.synchronizedMap(new HashMap<>());

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
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeCreateHelp());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);

        String homeName = args[0];
        if (!homeNamePattern.matcher(homeName).matches()) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeInvalidName());
            return true;
        }

        // Check if player is home-blocked (exceeds limit)
        int maxHomesSize = user.getMaxHomesSize();
        if (user.getProfile().getHomesSize() > maxHomesSize) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeMaxCapacity());
            return true;
        }

        // Check if we are replacing a home
        if (user.getProfile().getHome(homeName).isPresent()) {
            // Check if player already attempted to replace home recently
            HomeReplacement homeReplacement = replaceHomeAttempt.get(player.getUniqueId());
            if (homeReplacement != null) {
                // Check if the home is the same one he is attempting now
                if (homeReplacement.homeName().equalsIgnoreCase(homeName)) {
                    user.getProfile().removeHome(homeName);
                    user.getProfile().addHome(homeName, player.getLocation());
                    homeReplacement.task().cancel();
                    replaceHomeAttempt.remove(player.getUniqueId());
                    sender.sendMessage(plugin.getConfiguration().getTeleportHomeCreated());
                    return true;
                }
            }

            // Otherwise, insert him on the recent attempt map
            BukkitTask removeTask = plugin.getServer()
                                          .getScheduler()
                                          .runTaskLater(plugin,
                                                        () -> replaceHomeAttempt.remove(player.getUniqueId()),
                                                        plugin.getConfiguration().getHomeReplacementTimeout());
            HomeReplacement oldReplacement =
                    replaceHomeAttempt.put(player.getUniqueId(), new HomeReplacement(homeName, removeTask));
            // Cancel previous task if it exist
            if (oldReplacement != null) {
                oldReplacement.task().cancel();
            }
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeAlreadyExists());
            return true;
        }

        // Check if player can create a new home
        if (user.getProfile().getHomesSize() >= maxHomesSize) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeMaxCapacity());
            return true;
        }

        if (user.getProfile().addHome(homeName, player.getLocation())) {
            user.setTeleportTask(null);
            sender.sendMessage(plugin.getConfiguration().getTeleportHomeCreated());
            return true;
        }
        // This should not happen (existing home is already checked above)
        sender.sendMessage(plugin.getConfiguration().getTeleportHomeAlreadyExists());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        return List.of("casa", "base", "farm");
    }

    private record HomeReplacement(String homeName, BukkitTask task) {
    }
}
