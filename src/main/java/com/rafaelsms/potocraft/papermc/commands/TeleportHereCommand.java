package com.rafaelsms.potocraft.papermc.commands;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeleportHereCommand implements com.rafaelsms.potocraft.common.util.Command {

    private final @NotNull PaperPlugin plugin;

    public TeleportHereCommand(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String alias,
                             @NotNull String[] arguments) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] arguments) {
        return null;
    }
}
