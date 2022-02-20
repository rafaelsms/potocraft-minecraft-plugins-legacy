package com.rafaelsms.potocraft.blockprotection.commands;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.util.Selection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

        Optional<Selection> selectionOptional = plugin.getProtectionManager().getPlayerSelection(player);
        if (selectionOptional.isPresent()) {
            player.sendMessage("selection present, ready to make a protected region");
            return true;
        }

        if (plugin.getProtectionManager().startPlayerSelection(player)) {
            player.sendMessage("Started selection: click away");
            return true;
        }
        player.sendMessage("Selection restarted");
        return true;
    }
}
