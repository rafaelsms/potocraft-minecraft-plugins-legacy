package com.rafaelsms.potocraft.pet.commands;

import com.rafaelsms.potocraft.pet.Permissions;
import com.rafaelsms.potocraft.pet.PetPlugin;
import com.rafaelsms.potocraft.pet.player.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PetCommand implements CommandExecutor {

    private final @NotNull PetPlugin plugin;

    public PetCommand(@NotNull PetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnlyCommand());
            return true;
        }
        if (!sender.hasPermission(Permissions.PET_PERMISSION)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        User user = plugin.getUserManager().getUser(player);
        if (args.length == 0) {
            player.sendMessage(plugin.getConfiguration().getCommandHelp());
            // No arguments = Help + toggle
            boolean newEnabled = !user.getProfile().isPetEnabled();
            user.getProfile().setPetEnabled(newEnabled);
            if (newEnabled) {
                user.spawnPet();
                player.sendMessage(plugin.getConfiguration().getCommandPetEnabled());
            } else {
                user.despawnPet();
                player.sendMessage(plugin.getConfiguration().getCommandPetDisabled());
            }
            return true;
        }
        return true;
    }
}
