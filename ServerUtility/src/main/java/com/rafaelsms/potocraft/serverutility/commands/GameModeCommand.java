package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        // Check if label is a gamemode
        Optional<GameMode> gameModeFromLabel = gamemodeFromString(label);
        if (gameModeFromLabel.isEmpty()) {
            // There must be an argument specifying game mode, search on first argument
            if (args.length <= 0) {
                sender.sendMessage(plugin.getConfiguration().getGameModeHelp());
                return true;
            }
            Optional<GameMode> gameModeFromArgument = gamemodeFromString(args[0]);
            if (gameModeFromArgument.isEmpty()) {
                sender.sendMessage(plugin.getConfiguration().getGameModeHelp());
                return true;
            }
            handleGameMode(gameModeFromArgument.get(), sender, Util.offsetArray(args, 1).orElse(new String[0]));
        } else {
            handleGameMode(gameModeFromLabel.get(), sender, args);
        }
        return true;
    }

    private void handleGameMode(@NotNull GameMode gameMode, @NotNull CommandSender sender, @NotNull String[] args) {
        // Check if sender has permission to this gamemode
        if (!sender.hasPermission(gamemodePermissions.get(gameMode))) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return;
        }

        Player gamemodePlayer;
        if (args.length > 0) { // this args already excludes the possibly gamemode-related argument
            if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE_OTHERS)) {
                sender.sendMessage(plugin.getServer().getPermissionMessage());
                return;
            }
            Optional<? extends Player> optionalPlayer =
                    TextUtil.closestMatch(plugin.getServer().getOnlinePlayers(), Player::getName, args[0]);
            if (optionalPlayer.isEmpty()) {
                sender.sendMessage(plugin.getConfiguration().getPlayerNotFound());
                return;
            }
            gamemodePlayer = optionalPlayer.get();
        } else if (sender instanceof Player player) {
            gamemodePlayer = player;
        } else {
            sender.sendMessage(plugin.getConfiguration().getGameModeHelp());
            return;
        }

        // Finally, set gamemode for player
        gamemodePlayer.setGameMode(gameMode);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE)) {
            return List.of();
        }
        ArrayList<String> list = new ArrayList<>();
        if (gamemodeFromString(alias).isEmpty()) {
            list.addAll(List.of("creative", "spectator", "adventure", "survival"));
        }
        list.addAll(Util.convertList(plugin.getServer().getOnlinePlayers(), Player::getName));
        return list;
    }

    private Optional<GameMode> gamemodeFromString(@NotNull String string) {
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
