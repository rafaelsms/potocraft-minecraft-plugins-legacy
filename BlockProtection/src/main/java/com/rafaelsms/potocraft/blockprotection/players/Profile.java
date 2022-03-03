package com.rafaelsms.potocraft.blockprotection.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;
    private @NotNull String playerName;

    // Volume available for making regions
    private int volumeAvailable = 0;
    private double volumeParts = 0.0;

    public Profile(@NotNull UUID playerId, @NotNull String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public Profile(@NotNull Document document) {
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        this.playerName = document.getString(Keys.PLAYER_NAME);

        this.volumeAvailable = document.getInteger(Keys.VOLUME_AVAILABLE);
        this.volumeParts = document.getDouble(Keys.VOLUME_PARTS);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@NotNull String playerName) {
        this.playerName = playerName;
    }

    public long getVolumeAvailable() {
        movePartsToVolume();
        return volumeAvailable;
    }

    public void incrementVolume(double amount) {
        this.volumeParts += amount;
        movePartsToVolume();
    }

    public void consumeVolume(int volume) {
        if (volume < 0) {
            throw new IllegalArgumentException("Volume must be greater than zero");
        }
        this.volumeAvailable -= volume;
    }

    private void movePartsToVolume() {
        if (volumeParts < 1.0) {
            return;
        }
        int integer = (int) volumeParts;
        this.volumeParts = volumeParts - integer;
        this.volumeAvailable += integer;
    }

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
    }

    public Bson filterId() {
        return filterId(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
        document.put(Keys.PLAYER_NAME, playerName);
        document.put(Keys.VOLUME_AVAILABLE, volumeAvailable);
        document.put(Keys.VOLUME_PARTS, volumeParts);
        return document;
    }

    private static final class Keys {

        private static final String PLAYER_ID = "_id";
        private static final String PLAYER_NAME = "playerName";

        private static final String VOLUME_AVAILABLE = "volumeAvailable";
        private static final String VOLUME_PARTS = "volumeParts";

        // Private constructor
        private Keys() {
        }
    }
}
