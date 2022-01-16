package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.TeleportRequest;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TeleportDenyCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public TeleportDenyCommand(@NotNull ServerProfilePlugin plugin) {
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
        if (!sender.hasPermission(Permissions.TELEPORT)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);
        List<TeleportRequest> requests = user.getTeleportRequests();

        if (requests.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getTeleportRequestNotFound());
            return true;
        } else if (requests.size() == 1) {
            if (user.isPlayerTeleportBlocked(true)) {
                return true;
            }
            TeleportRequest request = requests.get(0);
            request.cancel();
            player.sendMessage(plugin.getConfiguration().getTeleportRequestCancelled());
            return true;
        } else {
            if (args.length == 1) {
                String name = args[0];
                Optional<TeleportRequest> requestOptional = TextUtil.closestStringMatch(requests,
                                                                                        request -> request
                                                                                                .getRequester()
                                                                                                .getPlayer()
                                                                                                .getName(),
                                                                                        name);
                if (requestOptional.isEmpty()) {
                    player.sendMessage(plugin.getConfiguration().getTeleportRequestManyFound(requests));
                    return true;
                }
                TeleportRequest request = requestOptional.get();
                request.cancel();
                player.sendMessage(plugin.getConfiguration().getTeleportRequestCancelled());
                return true;
            }
            player.sendMessage(plugin.getConfiguration().getTeleportRequestManyFound(requests));
            return true;
        }
    }
}
