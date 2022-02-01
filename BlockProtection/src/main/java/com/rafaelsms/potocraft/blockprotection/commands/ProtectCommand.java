package com.rafaelsms.potocraft.blockprotection.commands;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProtectCommand implements CommandExecutor {

    private final @NotNull BlockProtectionPlugin plugin;

    public ProtectCommand(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only in-game players can execute this command");
            return true;
        }

        if (plugin.getProtectionManager().addPlayerSelection(player)) {
            player.sendMessage("Started selection: click away");
            return true;
        }
        player.sendMessage("Selection restarted");
        return true;
    }
}
