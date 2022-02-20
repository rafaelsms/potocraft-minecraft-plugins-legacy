package com.rafaelsms.potocraft.blockprotection.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull BlockProtectionPlugin plugin;

    private final @NotNull UUID playerId;
    private String playerName;

    private @NotNull ZonedDateTime lastJoinDate;
    private double volumeAvailable;

    public Profile(@NotNull BlockProtectionPlugin plugin, @NotNull UUID playerId, @NotNull String playerName) {
        this.plugin = plugin;
        this.playerId = playerId;
        this.playerName = playerName;
        this.lastJoinDate = ZonedDateTime.now();
        this.volumeAvailable = plugin.getConfiguration().getDefaultVolume();
    }

    public Profile(@NotNull BlockProtectionPlugin plugin, @NotNull Document document) {
        this.plugin = plugin;
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        this.lastJoinDate = Util.convertNonNull(document.getString(Keys.LAST_JOIN_DATE), Util::toDateTime);
        this.volumeAvailable = document.getDouble(Keys.VOLUME_AVAILABLE);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setJoinDate() {
        this.lastJoinDate = ZonedDateTime.now();
    }

    public double getVolumeAvailable() {
        return volumeAvailable;
    }

    public void incrementVolume(double volume) {
        // Don't allow volume decrease through this method
        if (volume < 0.0) {
            return;
        }
        this.volumeAvailable += volume;
    }

    public static Bson filterId(UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convert(playerId, UUID::toString));
    }

    public Bson filterId() {
        return filterId(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
        document.put(Keys.PLAYER_NAME, playerName);

        document.put(Keys.LAST_JOIN_DATE, Util.convertNonNull(lastJoinDate, Util::fromDateTime));
        document.put(Keys.VOLUME_AVAILABLE, volumeAvailable);
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String PLAYER_NAME = "playerName";

        public static final String LAST_JOIN_DATE = "lastJoinDate";
        public static final String VOLUME_AVAILABLE = "volumeAvailable";

        // Private constructor
        private Keys() {
        }
    }
}
