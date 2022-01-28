package com.rafaelsms.potocraft.database.pojo;

import java.util.Map;
import java.util.UUID;

public class PlayerObject extends BaseObject {

    private UUID playerId;
    private String playerName;

    private ProxyProfile proxyProfile;
    private Map<String, ServerProfile> serverProfiles;

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

    public ProxyProfile getProxyProfile() {
        return proxyProfile;
    }

    public void setProxyProfile(ProxyProfile proxyProfile) {
        this.proxyProfile = proxyProfile;
    }

    public Map<String, ServerProfile> getServerProfiles() {
        return serverProfiles;
    }

    public void setServerProfiles(Map<String, ServerProfile> serverProfiles) {
        this.serverProfiles = serverProfiles;
    }
}
