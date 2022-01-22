package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerWeatherCommand implements CommandExecutor {

    private final @NotNull ServerUtilityPlugin plugin;

    public PlayerWeatherCommand(@NotNull ServerUtilityPlugin plugin) {
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
        if (!sender.hasPermission(Permissions.COMMAND_PLAYER_WEATHER)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getPlayerWeatherHelp());
            return true;
        }

        Optional<WeatherType> weatherType = parseWeather(args[0]);
        if (weatherType.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getPlayerWeatherHelp());
            player.resetPlayerWeather();
            return true;
        }

        player.setPlayerWeather(weatherType.get());
        return true;
    }

    private Optional<WeatherType> parseWeather(@NotNull String input) {
        return switch (input.toLowerCase()) {
            case "limpo", "clear" -> Optional.of(WeatherType.CLEAR);
            case "rain", "thunderstorm", "chuva", "chuvoso" -> Optional.of(WeatherType.DOWNFALL);
            default -> Optional.empty();
        };
    }
}
