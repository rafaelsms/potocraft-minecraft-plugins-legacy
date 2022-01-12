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
    private @Nullable Location deathLocation = null;

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

        this.backLocation = Location.fromDocument(document.get(Constants.BACK_LOCATION_KEY, Document.class));
        this.deathLocation = Location.fromDocument(document.get(Constants.DEATH_LOCATION_KEY, Document.class));
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public LinkedList<Home> getHomes() {
        LinkedList<Home> homes = new LinkedList<>(this.homes.values());
        homes.sort(Comparator.comparing(Home::getCreationTime));
        return homes;
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

    public Optional<Location> getDeathLocation() {
        return Optional.ofNullable(deathLocation);
    }

    public void setDeathLocation(@Nullable Location deathLocation) {
        this.deathLocation = deathLocation;
    }

    public static @NotNull ServerProfile fromDocument(@NotNull Document document) {
        return new ServerProfile(document);
    }

    public static @NotNull ServerProfile create(@NotNull UUID playerId) {
        return new ServerProfile(playerId);
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

        document.put(Constants.HOMES_KEY, Converter.toList(homes.values(), Home::toDocument));

        document.put(Constants.BACK_LOCATION_KEY, Location.toDocument(backLocation));
        document.put(Constants.DEATH_LOCATION_KEY, Location.toDocument(backLocation));

        return document;
    }

    private static final class Constants {

        public static final String PLAYER_ID_KEY = "_id";

        public static final String HOMES_KEY = "homes";

        public static final String BACK_LOCATION_KEY = "backLocation";
        public static final String DEATH_LOCATION_KEY = "deathLocation";

        private Constants() {
        }
    }
}
