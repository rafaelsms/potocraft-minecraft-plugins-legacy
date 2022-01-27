package com.rafaelsms.potocraft.serverprofile.commands.completers;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WarpNameCompleter implements TabCompleter {

    private final @NotNull ServerProfilePlugin plugin;

    public WarpNameCompleter(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.TELEPORT_WARP)) {
            return List.of();
        }
        return Util.convertList(plugin.getDatabase().getWarps().orElse(List.of()), Warp::getName);
    }
}
