package com.rafaelsms.potocraft.blockprotection.util;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Box extends DatabaseObject {

    private final @NotNull UUID worldId;
    private final int lowerX;
    private final int lowerY;
    private final int lowerZ;
    private final int higherX;
    private final int higherY;
    private final int higherZ;
    private final int volume;

    public Box(@NotNull Location lowerCorner, @NotNull Location higherCorner) {
        if (!lowerCorner.getWorld().getUID().equals(higherCorner.getWorld().getUID())) {
            throw new IllegalArgumentException("Locations of different worlds can't make a box!");
        }
        if (lowerCorner.getBlockX() > higherCorner.getBlockX() ||
            lowerCorner.getBlockY() > higherCorner.getBlockY() ||
            lowerCorner.getBlockZ() > higherCorner.getBlockZ()) {
            throw new IllegalArgumentException("Higher corner is lower than lower corner, can't make a box!");
        }
        this.worldId = lowerCorner.getWorld().getUID();
        this.lowerX = lowerCorner.getBlockX();
        this.lowerY = lowerCorner.getBlockY();
        this.lowerZ = lowerCorner.getBlockZ();
        this.higherX = higherCorner.getBlockX();
        this.higherY = higherCorner.getBlockY();
        this.higherZ = higherCorner.getBlockZ();
        this.volume = calculateVolume();
    }

    public Box(@NotNull Document document) {
        this.worldId = Util.convertNonNull(document.getString(Keys.WORLD_ID), UUID::fromString);
        this.lowerX = document.getInteger(Keys.LOWER_X);
        this.lowerY = document.getInteger(Keys.LOWER_Y);
        this.lowerZ = document.getInteger(Keys.LOWER_Z);
        this.higherX = document.getInteger(Keys.HIGHER_X);
        this.higherY = document.getInteger(Keys.HIGHER_Y);
        this.higherZ = document.getInteger(Keys.HIGHER_Z);
        this.volume = calculateVolume();
    }

    public @NotNull UUID getWorldId() {
        return worldId;
    }

    public int getLowerX() {
        return lowerX;
    }

    public int getLowerY() {
        return lowerY;
    }

    public int getLowerZ() {
        return lowerZ;
    }

    public int getHigherX() {
        return higherX;
    }

    public int getHigherY() {
        return higherY;
    }

    public int getHigherZ() {
        return higherZ;
    }

    public int getVolume() {
        return volume;
    }

    public boolean intersects(@NotNull Box otherBox) {
        return higherX >= otherBox.getLowerX() &&
               lowerX <= otherBox.getHigherX() &&
               higherY >= otherBox.getLowerY() &&
               lowerY <= otherBox.getHigherY() &&
               higherZ >= otherBox.getLowerZ() &&
               lowerZ <= otherBox.getHigherZ();
    }

    private int calculateVolume() {
        return (higherX - lowerX) * (higherY - lowerY) * (higherZ - lowerZ);
    }

    public @NotNull Bson filterColliding() {
        // Same as this#intersects(Box)
        return Filters.and(
                Filters.lte(Keys.LOWER_X, higherX),
                Filters.gte(Keys.HIGHER_X, lowerX),
                Filters.lte(Keys.LOWER_Y, higherY),
                Filters.gte(Keys.HIGHER_Y, lowerY),
                Filters.lte(Keys.LOWER_Z, higherZ),
                Filters.gte(Keys.HIGHER_Z, lowerZ)
        );
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.WORLD_ID, Util.convertNonNull(worldId, UUID::toString));
        document.put(Keys.LOWER_X, lowerX);
        document.put(Keys.LOWER_Y, lowerY);
        document.put(Keys.LOWER_Z, lowerZ);
        document.put(Keys.HIGHER_X, higherX);
        document.put(Keys.HIGHER_Y, higherY);
        document.put(Keys.HIGHER_Z, higherZ);
        return document;
    }

    private static final class Keys {

        public static final String WORLD_ID = "worldId";
        public static final String HIGHER_X = "higherX";
        public static final String HIGHER_Y = "higherY";
        public static final String HIGHER_Z = "higherZ";
        public static final String LOWER_X = "lowerX";
        public static final String LOWER_Y = "lowerY";
        public static final String LOWER_Z = "lowerZ";

        // Private constructor
        private Keys() {
        }
    }
}
