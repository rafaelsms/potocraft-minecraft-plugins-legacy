package com.rafaelsms.potocraft.papermc.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.util.Command;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.database.Home;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportTask;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HomeCommand implements Command {

    private final @NotNull PaperPlugin plugin;

    public HomeCommand(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             org.bukkit.command.@NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] arguments) {
        // Check permissions
        if (!sender.hasPermission(Permissions.TELEPORT_COMMAND_HOME)) {
            sender.sendMessage(plugin.getSettings().getNoPermission());
            return true;
        }

        // Ignore console
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return true;
        }

        // Check if exceeded argument length for command
        if (arguments.length > 2) {
            sender.sendMessage(plugin.getSettings().getHomeHelp());
            return true;
        }

        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
        Collection<Home> homes = user.getServerProfile().getHomes();

        // Check if player isn't attempting to teleport to its single home
        if (arguments.length == 0) {
            if (homes.size() != 1) {
                player.sendMessage(plugin.getSettings().getHomeHelp());
            } else {
                for (Home home : homes) {
                    teleportHome(user, home);
                    // Just to make sure >.>
                    break;
                }
            }
        } else {
            String argument = arguments[0];
            if (arguments.length > 1) {
                String homeName = arguments[1];
                com.rafaelsms.potocraft.common.profile.Location location =
                        com.rafaelsms.potocraft.common.profile.Location.fromPlayer(plugin.getSettings().getServerName(),
                                                                                   player.getLocation());
                if (argument.equalsIgnoreCase("criar")) {
                    if (user.getServerProfile().createHome(homeName, location)) {
                        player.sendMessage(plugin.getSettings().getHomeCreated(homeName));
                    } else {
                        player.sendMessage(plugin.getSettings().getHomeAlreadyExists());
                    }
                    return true;
                } else if (argument.equalsIgnoreCase("apagar")) {
                    if (user.getServerProfile().deleteHome(homeName)) {
                        player.sendMessage(plugin.getSettings().getHomeDeleted(homeName));
                    } else {
                        player.sendMessage(plugin.getSettings().getHomeAlreadyExists());
                    }
                    return true;
                } else if (argument.equalsIgnoreCase("substituir")) {
                    if (user.getServerProfile().deleteHome(homeName) &&
                        user.getServerProfile().createHome(homeName, location)) {
                        player.sendMessage(plugin.getSettings().getHomeCreated(homeName));
                    } else {
                        player.sendMessage(plugin.getSettings().getHomeList(homes));
                    }
                    return true;
                }
            }
            Optional<Home> optionalHome = TextUtil.closestStringMatch(homes, Home::getName, argument);
            if (optionalHome.isPresent()) {
                teleportHome(user, optionalHome.get());
            } else {
                player.sendMessage(plugin.getSettings().getHomeList(homes));
            }
        }
        return true;
    }

    private void teleportHome(@NotNull PaperUser user, @NotNull Home home) {
        // Check if home is available
        LinkedList<Home> homes = user.getServerProfile().getHomes();
        if (homes.size() > plugin.getSettings().getBaseHomeNumber() &&
            !user.getPlayer().hasPermission(Permissions.TELEPORT_COMMAND_HOME_UNLIMITED)) {

            // Find max number of homes that the player has access to
            int maxHomes = plugin.getSettings().getBaseHomeNumber();
            for (Map.Entry<String, Integer> entry : plugin.getSettings().getHomePermissionGroups().entrySet()) {
                // Skip if player doesn't have this group
                if (!user.getPlayer().hasPermission(entry.getKey())) {
                    continue;
                }
                // Update maximum if player has
                if (entry.getValue() > maxHomes) {
                    maxHomes = entry.getValue();
                }
            }

            // Don't allow if home is outside of range
            if ((homes.indexOf(home) + 1) > maxHomes) {
                user.getPlayer().sendMessage(plugin.getSettings().getHomeUnavailableNoPermission());
                return;
            }
        }

        // Check if player can teleport
        if (!user.getTeleportStatus().isAllowed()) {
            user.getPlayer().sendMessage(plugin.getSettings().getTeleportCanNotTeleportNow());
            return;
        }
        // Teleport player
        Location location = home.getLocation().toBukkit(plugin.getServer());
        TeleportTask teleportTask =
                TeleportTask.Builder.builder(plugin, user, location, PlayerTeleportEvent.TeleportCause.COMMAND).build();
        user.setTeleportTask(teleportTask);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                org.bukkit.command.@NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] arguments) {
        // Ignore console
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return null;
        }

        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
        Collection<Home> homes = user.getServerProfile().getHomes();
        if (arguments.length == 0) {
            List<String> list = new ArrayList<>();
            list.addAll(TextUtil.toStringList(homes, Home::getName));
            list.addAll(List.of("criar", "apagar", "substituir"));
            return list;
        } else if (arguments.length == 1) {
            return TextUtil.toStringList(homes, Home::getName);
        }
        return List.of();
    }
}
