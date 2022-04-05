package com.rafaelsms.teleporter.teleports;

public enum TeleportResult {

    SUCCESS, LOCATION_UNAVAILABLE, PLAYER_IN_COMBAT, ALREADY_TELEPORTING, TELEPORT_IN_COOLDOWN, PLAYER_OFFLINE;

    public boolean isFailed() {
        return this != SUCCESS;
    }
}
