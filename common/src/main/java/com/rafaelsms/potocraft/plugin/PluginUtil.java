package com.rafaelsms.potocraft.plugin;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PluginUtil {

    private PluginUtil() {
    }

    public static boolean isOnSurvival(@NotNull Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

}
