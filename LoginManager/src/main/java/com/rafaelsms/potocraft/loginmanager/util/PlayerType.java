package com.rafaelsms.potocraft.loginmanager.util;

import com.velocitypowered.api.proxy.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

public enum PlayerType {

    ONLINE_PLAYER,
    FLOODGATE_PLAYER,
    OFFLINE_PLAYER,
    ;

    public static @NotNull PlayerType get(@NotNull Player player) {
        if (FloodgateApi.getInstance().isFloodgateId(player.getUniqueId())) {
            return PlayerType.FLOODGATE_PLAYER;
        }
        if (player.isOnlineMode()) {
            return PlayerType.ONLINE_PLAYER;
        }
        return PlayerType.OFFLINE_PLAYER;
    }

    public boolean requiresLogin() {
        return this == OFFLINE_PLAYER;
    }

    @Override
    public String toString() {
        return switch (this) {
            case ONLINE_PLAYER -> "online player";
            case FLOODGATE_PLAYER -> "floodgate player";
            case OFFLINE_PLAYER -> "offline player";
        };
    }
}
