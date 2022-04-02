package com.rafaelsms.potocraft.database;

import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class LocationField extends CachedField<Location> {

    private final @NotNull JavaPlugin plugin;

    public LocationField(@Nullable String namespace,
                         @NotNull String key,
                         @NotNull CollectionProvider provider,
                         @Nullable Location defaultValue,
                         @NotNull JavaPlugin plugin) {
        super(namespace, key, provider, defaultValue);
        this.plugin = plugin;
    }

    public LocationField(@Nullable String namespace,
                         @NotNull String key,
                         @NotNull CollectionProvider provider,
                         @NotNull JavaPlugin plugin) {
        super(namespace, key, provider);
        this.plugin = plugin;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected <V> @Nullable V parseToDocument(@Nullable Location value) {
        if (value == null || value.getWorld() == null) {
            return null;
        }
        Document document = new Document();
        document.put(Keys.WORLD_ID, Util.convert(value.getWorld().getUID(), UUID::toString));
        document.put(Keys.X, value.getX());
        document.put(Keys.Y, value.getY());
        document.put(Keys.Z, value.getZ());
        document.put(Keys.YAW, String.valueOf(value.getYaw()));
        document.put(Keys.PITCH, String.valueOf(value.getPitch()));
        return (V) document;
    }

    @Override
    protected @Nullable Location parseFromDocument(@Nullable Object databaseObject, @Nullable Location defaultValue) {
        try {
            Document document = (Document) Optional.ofNullable(databaseObject).orElseThrow();
            UUID worldId = UUID.fromString(Optional.ofNullable(document.getString(Keys.WORLD_ID)).orElseThrow());
            World world = Optional.ofNullable(plugin.getServer().getWorld(worldId)).orElseThrow();
            double x = Optional.ofNullable(document.getDouble(Keys.X)).orElseThrow();
            double y = Optional.ofNullable(document.getDouble(Keys.Y)).orElseThrow();
            double z = Optional.ofNullable(document.getDouble(Keys.Z)).orElseThrow();
            float yaw = Float.parseFloat(Optional.ofNullable(document.getString(Keys.YAW)).orElseThrow());
            float pitch = Float.parseFloat(Optional.ofNullable(document.getString(Keys.PITCH)).orElseThrow());
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NoSuchElementException ignored) {
            return null;
        }
    }

    private static final class Keys {

        public static final String WORLD_ID = "worldId";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String Z = "z";
        public static final String YAW = "yaw";
        public static final String PITCH = "pitch";

    }
}
