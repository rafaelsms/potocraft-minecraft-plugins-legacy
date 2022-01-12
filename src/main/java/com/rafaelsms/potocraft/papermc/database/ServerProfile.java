package com.rafaelsms.potocraft.papermc.database;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.common.database.Converter;
import com.rafaelsms.potocraft.common.database.DatabaseObject;
import com.rafaelsms.potocraft.common.profile.Location;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.*;

public class ServerProfile extends DatabaseObject {

    private final @NotNull UUID playerId;

    private final Map<String, Home> homes = new HashMap<>();

    private @Nullable ZonedDateTime lastTeleportDate = null;
    private @Nullable Location backLocation = null;

    private ServerProfile(@NotNull UUID playerId) {
        super(new Document());
        this.playerId = playerId;
    }

    private ServerProfile(@NotNull Document document) {
        super(document);
        this.playerId = Objects.requireNonNull(Converter.toUUID(document.getString(Constants.PLAYER_ID_KEY)));
        // Parse homes
        List<Document> homeDocuments = document.getList(Constants.HOMES_KEY, Document.class);
        List<Home> homes = Converter.fromList(homeDocuments, Home::fromDocument);
        for (Home home : homes) {
            this.homes.put(home.getName().toLowerCase(), home);
        }
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public Collection<Home> getHomes() {
        return Collections.unmodifiableCollection(homes.values());
    }

    public boolean createHome(@NotNull String name, @NotNull Location location) {
        String key = name.toLowerCase();
        if (this.homes.containsKey(key)) {
            return false;
        }
        this.homes.put(key, Home.fromLocation(name, location));
        return true;
    }

    public boolean deleteHome(@NotNull String name) {
        return this.homes.remove(name.toLowerCase()) != null;
    }

    public Optional<ZonedDateTime> getLastTeleportDate() {
        return Optional.ofNullable(lastTeleportDate);
    }

    public void setLastTeleportDate() {
        this.lastTeleportDate = ZonedDateTime.now();
    }

    public Optional<Location> getBackLocation() {
        return Optional.ofNullable(backLocation);
    }

    public void setBackLocation(@Nullable Location backLocation) {
        this.backLocation = backLocation;
    }

    public static @NotNull ServerProfile fromDocument(@NotNull Document document) {
        return new ServerProfile(document);
    }

    public static @NotNull ServerProfile create(@NotNull UUID playerId) {
        return new ServerProfile(playerId);
    }

    public static @NotNull Document toDocument(@NotNull ServerProfile serverProfile) {
        return serverProfile.toDocument();
    }

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Constants.PLAYER_ID_KEY, Converter.fromUUID(playerId));
    }

    public Bson filterId() {
        return Filters.eq(Constants.PLAYER_ID_KEY, Converter.fromUUID(this.playerId));
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Constants.PLAYER_ID_KEY, Converter.fromUUID(playerId));
        document.put(Constants.HOMES_KEY, Converter.toList(homes.values(), DatabaseObject::toDocument));
        return document;
    }

    private static final class Constants {

        public static final String PLAYER_ID_KEY = "_id";

        public static final String HOMES_KEY = "homes";

        private Constants() {
        }
    }
}
