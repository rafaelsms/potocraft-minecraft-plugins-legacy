package com.rafaelsms.potocraft.combatserver.player;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;
    private @NotNull String playerName;

    private int armorUpgrades = 0;
    private int weaponUpgrades = 0;
    private int consumablesUpgrades = 0;

    public Profile(@NotNull UUID playerId, @NotNull String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public Profile(@NotNull Document document) {
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
        this.playerName = document.getString(Keys.PLAYER_NAME);
        this.armorUpgrades = document.getInteger(Keys.ARMOR_UPGRADES);
        this.weaponUpgrades = document.getInteger(Keys.WEAPON_UPGRADES);
        this.consumablesUpgrades = document.getInteger(Keys.CONSUMABLES_UPGRADES);
    }

    public void setPlayerName(@NotNull String playerName) {
        this.playerName = playerName;
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

        document.put(Keys.ARMOR_UPGRADES, armorUpgrades);
        document.put(Keys.WEAPON_UPGRADES, weaponUpgrades);
        document.put(Keys.CONSUMABLES_UPGRADES, consumablesUpgrades);
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String PLAYER_NAME = "playerName";

        public static final String ARMOR_UPGRADES = "armorUpgrades";
        public static final String WEAPON_UPGRADES = "weaponUpgrades";
        public static final String CONSUMABLES_UPGRADES = "consumablesUpgrades";

        // Private constructor
        private Keys() {
        }
    }
}
