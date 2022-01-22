package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerTimeCommand implements CommandExecutor {

    private final @NotNull ServerUtilityPlugin plugin;

    public PlayerTimeCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.COMMAND_PLAYER_TIME)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(plugin.getConfiguration().getPlayerTimeHelp());
            return true;
        }

        Optional<Long> optionalTime = parseTime(args[0]);
        if (optionalTime.isEmpty()) {
            if (player.isPlayerTimeRelative()) {
                player.resetPlayerTime();
                return true;
            }
            sender.sendMessage(plugin.getConfiguration().getPlayerTimeHelp());
            return true;
        }

        boolean fixed = args.length > 1;
        long offsetTime = player.getWorld().getTime() - optionalTime.get();

        player.setPlayerTime(offsetTime, !fixed);
        return true;
    }

    private Optional<Long> parseTime(@NotNull String input) {
        return switch (input.toLowerCase()) {
            case "meiodia", "noon", "midday" -> Optional.of(6_000L);
            case "day", "dia" -> Optional.of(0L);
            case "noite", "night" -> Optional.of(12_000L);
            case "meianoite", "midnight" -> Optional.of(18_000L);
            default -> Optional.empty();
        };
    }
}
