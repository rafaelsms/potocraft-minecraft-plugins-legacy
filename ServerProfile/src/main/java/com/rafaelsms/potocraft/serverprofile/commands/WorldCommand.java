package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerProfilePlugin plugin;

    public WorldCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return true;
        }
        if (!player.hasPermission(Permissions.TELEPORT_WORLD)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getTeleportWorldList(plugin.getServer().getWorlds()));
            return true;
        }

        World world = plugin.getServer().getWorld(args[0]);
        if (world != null) {
            User user = plugin.getUserManager().getUser(player);
            if (user.isPlayerTeleportBlocked(true)) {
                return true;
            }
            user.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }
        sender.sendMessage(plugin.getConfiguration().getTeleportWorldNotFound());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (!player.hasPermission(Permissions.TELEPORT_WORLD)) {
            return List.of();
        }
        return Util.convertList(plugin.getServer().getWorlds(), World::getName);
    }
}
