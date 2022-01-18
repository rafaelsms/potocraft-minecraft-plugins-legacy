package com.rafaelsms.potocraft.hardcore.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;

    private @Nullable ZonedDateTime deathDate = null;

    public Profile(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    public Profile(@NotNull Document document) {
        super(document);
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        this.deathDate = Util.convert(document.getString(Keys.DEATH_DATE), Util::toDateTime);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public Optional<ZonedDateTime> getDeathDate() {
        return Optional.ofNullable(deathDate);
    }

    public void setDeathDate() {
        this.deathDate = ZonedDateTime.now();
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
        document.put(Keys.DEATH_DATE, Util.fromDateTime(deathDate));
        return document;
    }

    public static Bson fromId(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
    }

    public Bson fromId() {
        return fromId(playerId);
    }

    private final static class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String DEATH_DATE = "lastDeathDate";

        // Private constructor
        private Keys() {
        }
    }
}
