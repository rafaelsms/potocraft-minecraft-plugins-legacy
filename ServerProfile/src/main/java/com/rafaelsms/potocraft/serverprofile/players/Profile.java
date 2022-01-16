package com.rafaelsms.potocraft.serverprofile.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.util.StoredLocation;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.*;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;

    private final Map<String, Home> homes = new HashMap<>();

    private @Nullable ZonedDateTime lastTeleportDate = null;
    private @Nullable StoredLocation backLocation = null;
    private @Nullable StoredLocation deathLocation = null;

    public Profile(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    public Profile(@NotNull Document document) {
        super(document);
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        List<Home> homes = Util.convertList(document.getList(Keys.HOMES, Document.class), Home::new);
        for (Home home : homes) {
            this.homes.put(home.getName().toLowerCase(), home);
        }
        this.lastTeleportDate = Util.toDateTime(document.getString(Keys.LAST_TELEPORT_DATE));
        this.backLocation = Util.convert(document.get(Keys.BACK_LOCATION, Document.class), StoredLocation::new);
        this.deathLocation = Util.convert(document.get(Keys.DEATH_LOCATION, Document.class), StoredLocation::new);
    }

    public @NotNull Map<String, Home> getHomes() {
        return Collections.unmodifiableMap(homes);
    }

    public boolean addHome(@NotNull String name, @NotNull Location location) {
        return homes.putIfAbsent(name.toLowerCase(), new Home(name, location)) == null;
    }

    public boolean removeHome(@NotNull String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public @NotNull Optional<ZonedDateTime> getLastTeleportDate() {
        return Optional.ofNullable(lastTeleportDate);
    }

    public void setLastTeleportDate() {
        this.lastTeleportDate = ZonedDateTime.now();
    }

    public @NotNull Optional<Location> getBackLocation(@NotNull ServerProfilePlugin plugin) {
        return Optional.ofNullable(Util.convert(backLocation, storedLocation -> storedLocation.toLocation(plugin)));
    }

    public void setBackLocation(@Nullable Location location) {
        this.backLocation = Util.convert(location, StoredLocation::new);
    }

    public @NotNull Optional<Location> getDeathLocation(@NotNull ServerProfilePlugin plugin) {
        return Optional.ofNullable(Util.convert(deathLocation, storedLocation -> storedLocation.toLocation(plugin)));
    }

    public void setDeathLocation(@Nullable Location location) {
        this.deathLocation = Util.convert(location, StoredLocation::new);
    }

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
    }

    public Bson filterId() {
        return filterId(playerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Profile profile = (Profile) o;
        return playerId.equals(profile.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(this.playerId, UUID::toString));
        document.put(Keys.HOMES, Util.convertList(this.homes.values(), Home::toDocument));
        document.put(Keys.LAST_TELEPORT_DATE, Util.fromDateTime(this.lastTeleportDate));
        document.put(Keys.BACK_LOCATION, Util.convert(backLocation, StoredLocation::toDocument));
        document.put(Keys.DEATH_LOCATION, Util.convert(deathLocation, StoredLocation::toDocument));
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String HOMES = "homeList";
        public static final String LAST_TELEPORT_DATE = "lastTeleportDate";
        public static final String BACK_LOCATION = "backLocation";
        public static final String DEATH_LOCATION = "deathLocation";

        // Private constructor
        private Keys() {
        }
    }
}
