package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TeleportCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public TeleportCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player teleportingPlayer)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.TELEPORT)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        // /tp <x> <y> <z>
        if (args.length == 3 && sender.hasPermission(Permissions.TELEPORT_POSITION)) {
            if (handleTeleportPosition(teleportingPlayer, args)) {
                return true;
            }
        }

        // Fallback to teleport command
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportHelp());
            return true;
        }

        // Find the closest player by the name
        String playerName = args[0];
        Optional<? extends Player> optionalPlayer =
                TextUtil.closestMatch(plugin.getServer().getOnlinePlayers(), Player::getName, playerName);
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getTeleportPlayerNotFound());
            return true;
        }
        Player destinationPlayer = optionalPlayer.get();
        if (destinationPlayer.getUniqueId().equals(teleportingPlayer.getUniqueId())) {
            sender.sendMessage(plugin.getConfiguration().getTeleportPlayerNotFound());
            return true;
        }

        // Teleport to player or request
        User teleportingUser = plugin.getUserManager().getUser(teleportingPlayer);
        if (sender.hasPermission(Permissions.TELEPORT_NO_REQUEST)) {
            teleportingUser.teleportNow(destinationPlayer.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }

        User destinationUser = plugin.getUserManager().getUser(destinationPlayer);
        User.TeleportRequestResponse requestResponse = destinationUser.addTeleportRequest(teleportingUser, true);
        switch (requestResponse) {
            case NEW_REQUEST -> {
                sender.sendMessage(plugin.getConfiguration().getTeleportRequestSent(destinationPlayer.getName()));
                destinationPlayer.sendMessage(plugin.getConfiguration()
                                                    .getTeleportRequestReceived(teleportingPlayer.getName()));
            }
            case NOT_UPDATED -> sender.sendMessage(plugin.getConfiguration()
                                                         .getTeleportRequestNotUpdated(destinationPlayer.getName()));
            case UPDATED -> sender.sendMessage(plugin.getConfiguration()
                                                     .getTeleportRequestSent(destinationPlayer.getName()));
        }
        return true;
    }

    private boolean handleTeleportPosition(Player player, String[] args) {
        assert args.length == 3;
        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);

            plugin.getUserManager()
                  .getUser(player)
                  .teleport(new Location(player.getWorld(), x, y, z), PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
