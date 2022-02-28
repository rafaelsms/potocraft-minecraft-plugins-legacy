package com.rafaelsms.potocraft.combatserver.arenas;

import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.StoredLocation;
import org.bson.Document;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Arena extends DatabaseObject {

    // Database properties
    private final @NotNull UUID arenaId = null;
    private @NotNull String arenaName;
    private @NotNull String displayName;
    private @NotNull StoredLocation corner1, corner2;

    // Runtime properties
    private @NotNull BoundingBox boundingBox;

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        return document;
    }

    private static final class Keys {

        private final static String ARENA_ID = "_id";
        private final static String ARENA_NAME = "name";
        private final static String DISPLAY_NAME = "displayName";
        private final static String CORNER_1 = "corner1";
        private final static String CORNER_2 = "corner2";

        // Private constructor
        private Keys() {
        }
    }
}
