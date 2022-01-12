package com.rafaelsms.potocraft.papermc.database;

import com.rafaelsms.potocraft.common.database.Converter;
import com.rafaelsms.potocraft.common.database.DatabaseObject;
import com.rafaelsms.potocraft.common.profile.Location;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Home extends DatabaseObject {

    private final @NotNull String name;
    private final @NotNull ZonedDateTime creationTime;
    private final @NotNull Location location;

    private Home(@NotNull String name, @NotNull Location location) {
        super(new Document());
        this.name = name;
        this.location = location;
        this.creationTime = ZonedDateTime.now();
    }

    private Home(@NotNull Document document) {
        super(document);
        this.name = document.getString(Constants.NAME_KEY);
        this.creationTime =
                Objects.requireNonNull(Converter.toDateTime(document.getString(Constants.CREATION_TIME_KEY)));
        this.location = Location.fromDocument(document.get(Constants.LOCATION_KEY, Document.class));
    }

    public @NotNull String getName() {
        return name;
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
        return name.equals(home.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static Home fromDocument(@NotNull Document document) {
        return new Home(document);
    }

    public static Home fromLocation(@NotNull String name, @NotNull Location location) {
        return new Home(name, location);
    }

    public static Document toDocument(@NotNull Home home) {
        return home.toDocument();
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Constants.NAME_KEY, this.name);
        document.put(Constants.CREATION_TIME_KEY, Objects.requireNonNull(Converter.fromDateTime(this.creationTime)));
        document.put(Constants.LOCATION_KEY, Location.toDocument(this.location));
        return document;
    }

    private static class Constants {

        public static final String NAME_KEY = "_id";
        public static final String CREATION_TIME_KEY = "creationTime";
        public static final String LOCATION_KEY = "location";

        private Constants() {
        }
    }
}
