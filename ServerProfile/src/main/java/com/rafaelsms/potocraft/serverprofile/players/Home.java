package com.rafaelsms.potocraft.serverprofile.players;

import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.util.StoredLocation;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Home extends DatabaseObject {

    private final @NotNull String name;
    private final @NotNull ZonedDateTime creationDate;
    private final @NotNull StoredLocation location;

    public Home(@NotNull String name, @NotNull Location location) {
        this.name = name;
        assert location.getWorld() != null;
        this.creationDate = ZonedDateTime.now();
        this.location = new StoredLocation(location);
    }

    public Home(@NotNull Document document) {
        super(document);
        this.name = document.getString(Keys.NAME);
        this.creationDate = Objects.requireNonNull(Util.toDateTime(document.getString(Keys.CREATION_DATE)));
        this.location = Util.convertNonNull(document.get(Keys.LOCATION, Document.class), StoredLocation::new);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public @NotNull Location toLocation(@NotNull ServerProfilePlugin plugin) {
        return location.toLocation(plugin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Home home = (Home) o;
        return name.equalsIgnoreCase(home.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.NAME, name);
        document.put(Keys.CREATION_DATE, Util.fromDateTime(creationDate));
        document.put(Keys.LOCATION, Util.convertNonNull(location, StoredLocation::toDocument));
        return document;
    }

    private static final class Keys {

        public static final String NAME = "name";
        public static final String CREATION_DATE = "creationDate";
        public static final String LOCATION = "location";

        // Private constructor
        private Keys() {
        }
    }
}
