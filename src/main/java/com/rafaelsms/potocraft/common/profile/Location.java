package com.rafaelsms.potocraft.common.profile;

import com.rafaelsms.potocraft.common.database.Converter;
import com.rafaelsms.potocraft.common.database.DatabaseObject;
import org.bson.Document;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Location extends DatabaseObject {

    private final @NotNull String serverName;
    private final @NotNull UUID worldId;
    private final double x, y, z;
    private final float pitch, yaw;

    private Location(@NotNull String serverName, @NotNull org.bukkit.Location location) {
        super(new Document());
        this.serverName = serverName;
        this.worldId = location.getWorld().getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    private Location(@NotNull Document document) {
        super(document);
        this.serverName = document.getString(Constants.SERVER_NAME_KEY);
        this.worldId = Converter.toUUID(document.getString(Constants.WORLD_ID_KEY));
        this.x = document.getDouble(Constants.X_KEY);
        this.y = document.getDouble(Constants.Y_KEY);
        this.z = document.getDouble(Constants.Z_KEY);
        this.yaw = Float.parseFloat(document.getString(Constants.YAW_KEY));
        this.pitch = Float.parseFloat(document.getString(Constants.PITCH_KEY));
    }

    public static @Nullable Location fromDocument(@Nullable Document document) {
        if (document == null) {
            return null;
        }
        return new Location(document);
    }

    public static @Nullable Location fromPlayer(@Nullable String serverName, @Nullable org.bukkit.Location location) {
        if (serverName == null || location == null) {
            return null;
        }
        return new Location(serverName, location);
    }

    public static @Nullable Document toDocument(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        return location.toDocument();
    }

    public @NotNull String getServerName() {
        return serverName;
    }

    public @NotNull org.bukkit.Location toBukkit(@NotNull Server server) {
        // World may be null
        return new org.bukkit.Location(server.getWorld(worldId), x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Constants.SERVER_NAME_KEY, serverName);
        document.put(Constants.WORLD_ID_KEY, Converter.fromUUID(worldId));
        document.put(Constants.X_KEY, x);
        document.put(Constants.Y_KEY, y);
        document.put(Constants.Z_KEY, z);
        document.put(Constants.YAW_KEY, String.valueOf(yaw));
        document.put(Constants.PITCH_KEY, String.valueOf(pitch));
        return document;
    }

    private static class Constants {
        public static final String SERVER_NAME_KEY = "serverName";
        public static final String WORLD_ID_KEY = "worldId";
        public static final String X_KEY = "x";
        public static final String Y_KEY = "y";
        public static final String Z_KEY = "z";
        public static final String YAW_KEY = "yaw";
        public static final String PITCH_KEY = "pitch";
    }
}
