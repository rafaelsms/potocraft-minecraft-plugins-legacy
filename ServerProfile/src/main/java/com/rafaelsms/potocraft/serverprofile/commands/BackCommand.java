package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BackCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public BackCommand(@NotNull ServerProfilePlugin plugin) {
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
        if (!player.hasPermission(Permissions.TELEPORT_BACK)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);
        Optional<Location> backLocation = user.getProfile().getBackLocation(plugin);
        if (backLocation.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getTeleportNoBackLocation());
            return true;
        }
        Optional<Location> deathLocation = user.getProfile().getDeathLocation(plugin);
        if (backLocation.get().equals(deathLocation.orElse(null)) &&
            !player.hasPermission(Permissions.TELEPORT_BACK_ON_DEATH)) {
            player.sendMessage(plugin.getConfiguration().getTeleportBackIsDeathLocation());
            return true;
        }
        if (user.isPlayerTeleportBlocked(true)) {
            return true;
        }
        user.teleport(backLocation.get(), PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }
}
