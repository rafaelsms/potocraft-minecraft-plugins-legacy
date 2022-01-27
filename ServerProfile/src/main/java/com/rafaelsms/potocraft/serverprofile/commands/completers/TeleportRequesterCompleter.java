package com.rafaelsms.potocraft.serverprofile.commands.completers;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.TeleportRequest;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeleportRequesterCompleter implements TabCompleter {

    private final @NotNull ServerProfilePlugin plugin;

    public TeleportRequesterCompleter(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (!sender.hasPermission(Permissions.TELEPORT)) {
            return List.of();
        }

        User user = plugin.getUserManager().getUser(player);
        List<TeleportRequest> requests = user.getTeleportRequests();
        return Util.convertList(requests, request -> request.getRequester().getPlayer().getName());
    }
}
