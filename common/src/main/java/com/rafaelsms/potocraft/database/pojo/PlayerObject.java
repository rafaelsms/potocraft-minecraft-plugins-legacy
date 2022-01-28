package com.rafaelsms.potocraft.database.pojo;

import java.util.UUID;

public abstract class PlayerObject extends BaseObject {

    private UUID playerId;
    private String playerName;

    public PlayerObject() {
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
