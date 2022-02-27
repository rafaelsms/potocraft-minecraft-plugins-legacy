package com.rafaelsms.potocraft.pet.player;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;
    private @NotNull String playerName;

    private @Nullable EntityType petType = null;
    private @Nullable String petColoredName = null;
    private boolean petEnabled = true;
    private boolean petBaby = true;

    // TODO limit pet changes by date using permissions

    public Profile(@NotNull UUID playerId, @NotNull String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public Profile(@NotNull Document document) {
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        this.playerName = document.getString(Keys.PLAYER_NAME);

        this.petType = Util.convert(document.getString(Keys.PET_ENTITY_TYPE),
                                    string -> EntityType.valueOf(string.toUpperCase()));
        this.petColoredName = document.getString(Keys.PET_COLORED_NAME);
        this.petEnabled = document.getBoolean(Keys.PET_ENABLED, petEnabled);
        this.petBaby = document.getBoolean(Keys.PET_BABY, petBaby);
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@NotNull String playerName) {
        this.playerName = playerName;
    }

    public Optional<EntityType> getPetType() {
        return Optional.ofNullable(petType);
    }

    public void setPetType(@NotNull EntityType petType) {
        this.petType = petType;
    }

    public Optional<String> getPetColoredName() {
        return Optional.ofNullable(petColoredName);
    }

    public void setPetColoredName(@NotNull String petColoredName) {
        this.petColoredName = petColoredName;
    }

    public boolean isPetEnabled() {
        return petEnabled;
    }

    public void setPetEnabled(boolean petEnabled) {
        this.petEnabled = petEnabled;
    }

    public boolean isPetBaby() {
        return petBaby;
    }

    public void setPetBaby(boolean petBaby) {
        this.petBaby = petBaby;
    }

    public static Bson filterById(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
    }

    public Bson filterById() {
        return filterById(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
        document.put(Keys.PLAYER_NAME, playerName);

        document.put(Keys.PET_ENTITY_TYPE, Util.convert(petType, EntityType::name));
        document.put(Keys.PET_COLORED_NAME, petColoredName);
        document.put(Keys.PET_ENABLED, petEnabled);
        document.put(Keys.PET_BABY, petBaby);
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String PLAYER_NAME = "playerName";

        public static final String PET_ENTITY_TYPE = "petEntityTypeName";
        public static final String PET_COLORED_NAME = "petColoredName";
        public static final String PET_ENABLED = "petIsEnabled";
        public static final String PET_BABY = "petIsBaby";

        // Private constructor
        private Keys() {
        }
    }
}
