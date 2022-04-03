package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class LocationSerializer implements Serializer<Location> {

    private static final String WORLD_KEY = "worldId";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";
    private static final String YAW_KEY = "yaw";
    private static final String PITCH_KEY = "pitch";

    private static final Serializer<Float> FLOAT_SERIALIZER = FloatSerializer.getSerializer();

    private final @NotNull JavaPlugin plugin;

    private LocationSerializer(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static Serializer<Location> getSerializer(@NotNull JavaPlugin plugin) {
        return new LocationSerializer(plugin);
    }

    @Override
    public @Nullable Location fromDocument(@Nullable Object object) {
        try {
            Document document = Util.convertOrThrow(object, Document.class);
            UUID worldId = UUID.fromString(Util.nonNull(document.getString(WORLD_KEY)));
            World world = Util.nonNull(plugin.getServer().getWorld(worldId));
            double x = Util.nonNull(document.getDouble(X_KEY));
            double y = Util.nonNull(document.getDouble(Y_KEY));
            double z = Util.nonNull(document.getDouble(Z_KEY));
            float yaw = Optional.ofNullable(FLOAT_SERIALIZER.fromDocument(document.get(YAW_KEY))).orElse(0f);
            float pitch = Optional.ofNullable(FLOAT_SERIALIZER.fromDocument(document.get(PITCH_KEY))).orElse(0f);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    @Override
    public @Nullable Object toDocument(@Nullable Location object) {
        if (object == null) {
            return null;
        }
        Document document = new Document();
        document.put(WORLD_KEY, Util.nonNull(object.getWorld()).getUID().toString());
        document.put(X_KEY, object.getX());
        document.put(Y_KEY, object.getY());
        document.put(Z_KEY, object.getZ());
        document.put(YAW_KEY, FLOAT_SERIALIZER.toDocument(object.getYaw()));
        document.put(PITCH_KEY, FLOAT_SERIALIZER.toDocument(object.getPitch()));
        return document;
    }
}
