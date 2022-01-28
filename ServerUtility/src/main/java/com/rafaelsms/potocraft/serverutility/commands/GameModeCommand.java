package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameModeCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerUtilityPlugin plugin;

    private final Map<GameMode, String> gamemodePermissions = Map.of(GameMode.SURVIVAL,
                                                                     Permissions.COMMAND_GAMEMODE_SURVIVAL,
                                                                     GameMode.CREATIVE,
                                                                     Permissions.COMMAND_GAMEMODE_CREATIVE,
                                                                     GameMode.ADVENTURE,
                                                                     Permissions.COMMAND_GAMEMODE_ADVENTURE,
                                                                     GameMode.SPECTATOR,
                                                                     Permissions.COMMAND_GAMEMODE_SPECTATOR);

    public GameModeCommand(@NotNull ServerUtilityPlugin plugin) {
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
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        String gameModeString = label;
        if (args.length > 0) {
            gameModeString = args[0];
        }

        Optional<GameMode> gameModeOptional = gamemodeFromString(gameModeString);
        if (gameModeOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getGameModeHelp());
            return true;
        }
        GameMode gameMode = gameModeOptional.get();
        if (player.hasPermission(gamemodePermissions.get(gameMode))) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        player.setGameMode(gameMode);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE)) {
            return List.of();
        }
        return List.of("creative", "spectator", "adventure", "survival");
    }

    private Optional<GameMode> gamemodeFromString(String string) {
        string = string.toLowerCase();
        if (startsOrEndsWith(string, "a")) {
            return Optional.of(GameMode.ADVENTURE);
        }
        if (startsOrEndsWith(string, "sp")) {
            return Optional.of(GameMode.SPECTATOR);
        }
        if (startsOrEndsWith(string, "s")) {
            return Optional.of(GameMode.SURVIVAL);
        }
        if (startsOrEndsWith(string, "c")) {
            return Optional.of(GameMode.CREATIVE);
        }
        return Optional.empty();
    }

    private boolean startsOrEndsWith(@NotNull String string, @NotNull String prefixSuffix) {
        return string.startsWith(prefixSuffix) || string.endsWith(prefixSuffix);
    }
}
