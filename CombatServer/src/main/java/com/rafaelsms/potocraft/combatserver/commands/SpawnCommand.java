package com.rafaelsms.potocraft.combatserver.commands;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import com.rafaelsms.potocraft.combatserver.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final @NotNull CombatServerPlugin plugin;

    public SpawnCommand(@NotNull CombatServerPlugin plugin) {
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
        if (!sender.hasPermission(Permissions.SPAWN_COMMAND)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        // Teleport to spawn and clear inventory if player doesn't have permissions
        player.teleportAsync(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND)
              .whenComplete((success, throwable) -> {
                  if (!player.isOnline()) {
                      return;
                  }
                  if (!Boolean.TRUE.equals(success)) {
                      player.sendMessage(plugin.getConfiguration().getSomethingWentWrong());
                      return;
                  }
                  if (!player.hasPermission(Permissions.SPAWN_COMMAND_CLEAR_BYPASS)) {
                      // Clear player's inventory on teleport
                      player.getInventory().clear();
                  }
              });
        return true;
    }
}
