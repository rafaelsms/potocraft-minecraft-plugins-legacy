package com.rafaelsms.potocraft.util;

public enum PlayerType {

    ONLINE_PLAYER,
    FLOODGATE_PLAYER,
    OFFLINE_PLAYER,
    ;

    @Override
    public String toString() {
        return switch (this) {
            case OFFLINE_PLAYER -> "offline player";
            case ONLINE_PLAYER -> "online player";
            case FLOODGATE_PLAYER -> "floodgate player";
        };
    }
}
