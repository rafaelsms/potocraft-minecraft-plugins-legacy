package com.rafaelsms.potocraft.blockprotection.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull BlockProtectionPlugin plugin;
    private final @NotNull UUID playerId;

    private @NotNull ZonedDateTime lastJoinDate;
    private double volumeAvailable;

    public Profile(@NotNull BlockProtectionPlugin plugin, @NotNull UUID playerId) {
        this.plugin = plugin;
        this.playerId = playerId;
        this.lastJoinDate = ZonedDateTime.now();
        this.volumeAvailable = plugin.getConfiguration().getDefaultVolume();
    }

    public Profile(@NotNull BlockProtectionPlugin plugin, @NotNull Document document) {
        this.plugin = plugin;
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID_KEY), UUID::fromString);
        this.lastJoinDate = Util.convertNonNull(document.getString(Keys.LAST_JOIN_DATE_KEY), Util::toDateTime);
        this.volumeAvailable = document.getDouble(Keys.VOLUME_AVAILABLE);
    }

    public void setJoinDate() {
        this.lastJoinDate = ZonedDateTime.now();
    }

    public double getVolumeAvailable() {
        return volumeAvailable;
    }

    public void incrementVolume(double volume, int maximumVolume) {
        // Don't decrease, but don't increase it either
        if (this.volumeAvailable >= maximumVolume) {
            return;
        }
        this.volumeAvailable += volume;
    }

    public static Bson filterId(UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID_KEY, Util.convert(playerId, UUID::toString));
    }

    public Bson filterId() {
        return filterId(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID_KEY, Util.convertNonNull(playerId, UUID::toString));
        document.put(Keys.LAST_JOIN_DATE_KEY, Util.convertNonNull(lastJoinDate, Util::fromDateTime));
        document.put(Keys.VOLUME_AVAILABLE, volumeAvailable);
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID_KEY = "_id";
        public static final String LAST_JOIN_DATE_KEY = "lastJoinDate";
        public static final String VOLUME_AVAILABLE = "volumeAvailable";

        // Private constructor
        private Keys() {
        }
    }
}
