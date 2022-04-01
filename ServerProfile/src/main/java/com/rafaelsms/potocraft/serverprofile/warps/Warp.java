package com.rafaelsms.potocraft.serverprofile.warps;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseStored;
import com.rafaelsms.potocraft.database.LocationField;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Warp extends DatabaseStored {

    private final @NotNull String name;
    private final @NotNull LocationField location;

    public Warp(@NotNull Document document) {
        super(document);
        this.name = document.getString(Keys.NAME);
        this.location = new LocationField(document.get(Keys.LOCATION, Document.class));
    }

    public Warp(@NotNull String name, @NotNull Location location) {
        super();
        this.name = name.toLowerCase();
        this.location = new LocationField(location);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Optional<Location> getLocation(@NotNull ServerProfilePlugin plugin) {
        return location.toLocation(plugin);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.NAME, name);
        document.put(Keys.LOCATION, location.toDocument());
        return document;
    }

    public static Bson filterName(@NotNull String name) {
        return Filters.eq(Keys.NAME, name.toLowerCase());
    }

    public Bson filterName() {
        return Filters.eq(Keys.NAME, name);
    }

    private static final class Keys {

        public static final String NAME = "_id";
        public static final String LOCATION = "location";

        // Private constructor
        private Keys() {
        }
    }
}
