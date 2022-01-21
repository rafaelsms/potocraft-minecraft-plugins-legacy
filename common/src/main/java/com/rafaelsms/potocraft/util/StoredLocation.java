package com.rafaelsms.potocraft.util;

import com.rafaelsms.potocraft.database.DatabaseObject;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StoredLocation extends DatabaseObject {

    private final @NotNull UUID worldId;
    private final double x, y, z;

    public StoredLocation(@NotNull Location location) {
        assert location.getWorld() != null;
        this.worldId = location.getWorld().getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public StoredLocation(@NotNull Document document) {
        super(document);
        this.worldId = Util.convertNonNull(document.getString(Keys.WORLD_ID), UUID::fromString);
        this.x = document.getDouble(Keys.X_KEY);
        this.y = document.getDouble(Keys.Y_KEY);
        this.z = document.getDouble(Keys.Z_KEY);
    }

    public @NotNull Location toLocation(@NotNull Plugin plugin) {
        return new Location(plugin.getServer().getWorld(worldId), x, y, z);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.WORLD_ID, Util.convert(worldId, UUID::toString));
        document.put(Keys.X_KEY, x);
        document.put(Keys.Y_KEY, y);
        document.put(Keys.Z_KEY, z);
        return document;
    }

    private static final class Keys {

        public static final String WORLD_ID = "worldId";
        public static final String X_KEY = "x";
        public static final String Y_KEY = "y";
        public static final String Z_KEY = "z";

        // Private constructor
        private Keys() {
        }
    }
}
