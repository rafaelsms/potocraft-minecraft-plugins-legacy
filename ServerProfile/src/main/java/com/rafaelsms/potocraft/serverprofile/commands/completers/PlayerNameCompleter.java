package com.rafaelsms.potocraft.serverprofile.commands.completers;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.util.ServerUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerNameCompleter implements TabCompleter {

    private final @NotNull ServerProfilePlugin plugin;
    private final @NotNull String permissionRequired;

    public PlayerNameCompleter(@NotNull ServerProfilePlugin plugin, @NotNull String permissionRequired) {
        this.plugin = plugin;
        this.permissionRequired = permissionRequired;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        if (!sender.hasPermission(permissionRequired)) {
            return List.of();
        }
        return Util.convertList(ServerUtil.getVisiblePlayers(plugin), Player::getName);
    }
}
