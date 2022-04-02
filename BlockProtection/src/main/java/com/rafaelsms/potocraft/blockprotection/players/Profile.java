package com.rafaelsms.potocraft.blockprotection.players;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.database.CachedField;
import com.rafaelsms.potocraft.database.CachedMapField;
import com.rafaelsms.potocraft.database.CachedSimpleField;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.IdentifiedDatabaseStored;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Profile extends IdentifiedDatabaseStored {

    private final @NotNull BlockProtectionPlugin plugin;

    private final CachedMapField<String> regionNames;
    private final CachedSimpleField<Integer> areaAvailable;
    private final CachedSimpleField<Double> areaParts;

    private Profile(@NotNull BlockProtectionPlugin plugin, @NotNull UUID playerId) throws DatabaseException {
        super(playerId);
        this.plugin = plugin;
        this.regionNames = new CachedMapField<>("regionMap", collectionProvider);
        this.areaAvailable = new CachedSimpleField<>("areaAvailable", collectionProvider, 0);
        this.areaParts = new CachedSimpleField<>("areaParts", collectionProvider, 0.0);
        fetchAllFields().ifPresent(document -> {
            this.regionNames.readFrom(document);
            this.areaAvailable.readFrom(document);
            this.areaParts.readFrom(document);
        });
    }

    public static Profile fetch(@NotNull BlockProtectionPlugin plugin, @NotNull UUID playerId) throws
                                                                                               DatabaseException {
        return new Profile(plugin, playerId);
    }

    @Override
    protected List<CachedField<?>> getOtherRegisteredFields() {
        return List.of(regionNames, areaAvailable, areaParts);
    }

    @Override
    protected MongoCollection<Document> getMongoCollection() throws DatabaseException {
        return plugin.getDatabase().getPlayerCollection();
    }

    private int getAreaAvailableInternal() {
        return areaAvailable.getOrDefault().orElseThrow();
    }

    private double getAreaPartsInternal() {
        return this.areaParts.getOrDefault().orElseThrow();
    }

    public int getAreaAvailable() {
        movePartsToArea();
        return getAreaAvailableInternal();
    }

    public void incrementArea(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
        this.areaParts.set(getAreaPartsInternal() + amount);
        movePartsToArea();
    }

    public void consumeArea(int area) {
        if (area < 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
        this.areaAvailable.set(getAreaAvailable() - area);
    }

    private void movePartsToArea() {
        double areaParts = getAreaPartsInternal();
        if (areaParts < 1.0) {
            return;
        }
        int integer = (int) areaParts;
        this.areaParts.set(areaParts - integer);
        this.areaAvailable.set(getAreaAvailableInternal() + integer);
    }

    public boolean isRegionOwner(@NotNull String regionId) {
        // Compare ignoring case
        for (String otherRegionId : regionNames.values()) {
            if (regionId.equalsIgnoreCase(otherRegionId)) {
                return true;
            }
        }
        return false;
    }

    public void addCreatedRegion(@NotNull String regionName, @NotNull String regionId) {
        this.regionNames.put(regionName, regionId);
    }
}
