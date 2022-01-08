package com.rafaelsms.potocraft.papermc.util;

import com.rafaelsms.potocraft.util.Converter;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PaperConverter {

    public static Document fromLocation(@Nullable Location location) {
        if (location == null) return null;
        UUID worldId;
        if (location.getWorld() == null)
            worldId = null;
        else
            worldId = location.getWorld().getUID();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        String yaw = String.valueOf(location.getYaw());
        String pitch = String.valueOf(location.getYaw());

        Document document = new Document();
        document.put(LocationConstants.WORLD_KEY, Converter.fromUUID(worldId));
        document.put(LocationConstants.X_KEY, x);
        document.put(LocationConstants.Y_KEY, y);
        document.put(LocationConstants.Z_KEY, z);
        document.put(LocationConstants.YAW_KEY, yaw);
        document.put(LocationConstants.PITCH_KEY, pitch);
        return document;
    }

    public static Location toLocation(@NotNull JavaPlugin plugin, @Nullable Document document) {
        if (document == null) return null;
        UUID worldId = Converter.toUUID(document.getString(LocationConstants.WORLD_KEY));
        World world;
        if (worldId == null)
            world = null;
        else
            world = plugin.getServer().getWorld(worldId);
        double x = document.getDouble(LocationConstants.X_KEY);
        double y = document.getDouble(LocationConstants.Y_KEY);
        double z = document.getDouble(LocationConstants.Z_KEY);
        float yaw = Float.parseFloat(document.getString(LocationConstants.YAW_KEY));
        float pitch = Float.parseFloat(document.getString(LocationConstants.PITCH_KEY));
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static class LocationConstants {
        private static final String WORLD_KEY = "world";
        private static final String X_KEY = "x";
        private static final String Y_KEY = "y";
        private static final String Z_KEY = "z";
        private static final String YAW_KEY = "yaw";
        private static final String PITCH_KEY = "pitch";
    }

}
