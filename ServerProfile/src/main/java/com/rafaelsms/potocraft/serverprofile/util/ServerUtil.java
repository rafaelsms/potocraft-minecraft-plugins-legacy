package com.rafaelsms.potocraft.serverprofile.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ServerUtil {

    // Private constructor
    private ServerUtil() {
    }

    public static List<Player> getVisiblePlayers(@NotNull JavaPlugin plugin) {
        List<Player> players = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (player.isInvisible()) {
                continue;
            }
            players.add(player);
        }
        return players;
    }
}
