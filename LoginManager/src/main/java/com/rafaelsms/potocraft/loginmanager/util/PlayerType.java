package com.rafaelsms.potocraft.loginmanager.util;

public enum PlayerType {

    ONLINE_PLAYER, FLOODGATE_PLAYER, OFFLINE_PLAYER;

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
