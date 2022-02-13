package com.rafaelsms.potocraft.gameserver.util;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GameUtil {

    // Private constructor
    private GameUtil() {
    }

    public static int countGameMode(@NotNull World world, GameMode gameMode) {
        int spectators = 0;
        for (Player player : world.getPlayers()) {
            if (player.getGameMode() == gameMode) {
                spectators++;
            }
        }
        return spectators;
    }

}
