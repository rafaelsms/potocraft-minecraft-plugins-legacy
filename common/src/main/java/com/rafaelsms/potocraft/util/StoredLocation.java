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
    private final float yaw, pitch;

    public StoredLocation(@NotNull Location location) {
        assert location.getWorld() != null;
        this.worldId = location.getWorld().getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public StoredLocation(@NotNull Document document) {
        super(document);
        this.worldId = Util.convertNonNull(document.getString(Keys.WORLD_ID), UUID::fromString);
        this.x = document.getDouble(Keys.X_KEY);
        this.y = document.getDouble(Keys.Y_KEY);
        this.z = document.getDouble(Keys.Z_KEY);
        this.yaw = Util.getOrElse(Util.convert(document.getString(Keys.YAW_KEY), Float::parseFloat), 0.0f);
        this.pitch = Util.getOrElse(Util.convert(document.getString(Keys.PITCH_KEY), Float::parseFloat), 0.0f);
        ;
    }

    public @NotNull Location toLocation(@NotNull Plugin plugin) {
        return new Location(plugin.getServer().getWorld(worldId), x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.WORLD_ID, Util.convert(worldId, UUID::toString));
        document.put(Keys.X_KEY, x);
        document.put(Keys.Y_KEY, y);
        document.put(Keys.Z_KEY, z);
        document.put(Keys.YAW_KEY, String.valueOf(yaw));
        document.put(Keys.PITCH_KEY, String.valueOf(pitch));
        return document;
    }

    private static final class Keys {

        public static final String WORLD_ID = "worldId";
        public static final String X_KEY = "x";
        public static final String Y_KEY = "y";
        public static final String Z_KEY = "z";
        public static final String YAW_KEY = "yaw";
        public static final String PITCH_KEY = "pitch";

        // Private constructor
        private Keys() {
        }
    }
}
