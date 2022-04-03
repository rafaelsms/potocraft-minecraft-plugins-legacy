package com.rafaelsms.teleporter.database;

import com.rafaelsms.potocraft.database.serializers.DateTimeSerializer;
import com.rafaelsms.potocraft.database.serializers.LocationSerializer;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.teleporter.player.Home;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public class HomeSerializer implements Serializer<Home> {

    private static final String LOCATION_KEY = "location";
    private static final String CREATION_DATE_KEY = "creationDateTime";

    private final Serializer<Location> locationSerializer;
    private final Serializer<ZonedDateTime> dateTimeSerializer;

    private HomeSerializer(@NotNull JavaPlugin plugin) {
        this.locationSerializer = LocationSerializer.getSerializer(plugin);
        this.dateTimeSerializer = DateTimeSerializer.getSerializer();
    }

    public static Serializer<Home> getSerializer(@NotNull JavaPlugin plugin) {
        return new HomeSerializer(plugin);
    }

    @Override
    public @Nullable Home fromDocument(@Nullable Object object) {
        try {
            Document document = Util.convertOrThrow(object, Document.class);
            Location location = Util.convertOrThrow(document.get(LOCATION_KEY), locationSerializer::fromDocument);
            ZonedDateTime dateTime =
                    Util.convertOrThrow(document.get(CREATION_DATE_KEY), dateTimeSerializer::fromDocument);
            return new Home(location, dateTime);
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    @Override
    public @Nullable Object toDocument(@Nullable Home object) {
        try {
            Home home = Util.nonNull(object);
            Document document = new Document();
            document.put(LOCATION_KEY, Util.nonNull(locationSerializer.toDocument(home.getLocation())));
            document.put(CREATION_DATE_KEY, Util.nonNull(dateTimeSerializer.toDocument(home.getCreationDate())));
            return document;
        } catch (NullPointerException ignored) {
            return null;
        }
    }
}
