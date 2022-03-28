package com.rafaelsms.potocraft.blockprotection.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;
    private @NotNull String playerName;

    private final Map<String, String> regionNameId = new HashMap<>();

    // Area available for making regions
    private int areaAvailable = 0;
    private double areaParts = 0.0;

    public Profile(@NotNull UUID playerId, @NotNull String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public Profile(@NotNull Document document) {
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        this.playerName = document.getString(Keys.PLAYER_NAME);

        Document regionMap = document.get(Keys.CREATED_REGION_IDS, Document.class);
        if (regionMap != null) {
            for (Map.Entry<String, Object> entry : regionMap.entrySet()) {
                regionNameId.put(entry.getKey(), (String) entry.getValue());
            }
        }

        this.areaAvailable = Objects.requireNonNullElseGet(document.getInteger(Keys.AREA_AVAILABLE),
                                                           () -> document.getInteger(Keys.VOLUME_AVAILABLE) / 8);
        this.areaParts = Objects.requireNonNullElseGet(document.getDouble(Keys.AREA_PARTS),
                                                       () -> document.getDouble(Keys.VOLUME_PARTS) / 8.0);
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

    public int getAreaAvailable() {
        movePartsToArea();
        return areaAvailable;
    }

    public void incrementArea(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
        this.areaParts += amount;
        movePartsToArea();
    }

    public void consumeArea(int area) {
        if (area < 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
        this.areaAvailable -= area;
    }

    private void movePartsToArea() {
        if (areaParts < 1.0) {
            return;
        }
        int integer = (int) areaParts;
        this.areaParts = areaParts - integer;
        this.areaAvailable += integer;
    }

    public boolean isRegionOwner(@NotNull String regionId) {
        // Compare ignoring case
        for (String otherRegionId : this.regionNameId.values()) {
            if (regionId.equalsIgnoreCase(otherRegionId)) {
                return true;
            }
        }
        return false;
    }

    public void addCreatedRegion(@NotNull String regionName, @NotNull String regionId) {
        this.regionNameId.put(regionName, regionId);
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

        Document regionMap = new Document();
        regionMap.putAll(regionNameId);
        document.put(Keys.CREATED_REGION_IDS, regionMap);

        document.put(Keys.AREA_AVAILABLE, areaAvailable);
        document.put(Keys.AREA_PARTS, areaParts);
        return document;
    }

    private static final class Keys {

        private static final String PLAYER_ID = "_id";
        private static final String PLAYER_NAME = "playerName";

        private static final String CREATED_REGION_IDS = "regionMap";

        private static final String VOLUME_AVAILABLE = "volumeAvailable";
        private static final String VOLUME_PARTS = "volumeParts";
        private static final String AREA_AVAILABLE = "areaAvailable";
        private static final String AREA_PARTS = "areaParts";

        // Private constructor
        private Keys() {
        }
    }
}
