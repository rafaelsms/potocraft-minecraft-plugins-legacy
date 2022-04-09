package com.rafaelsms.teleporter.player;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.fields.CachedField;
import com.rafaelsms.potocraft.database.fields.CachedMapField;
import com.rafaelsms.potocraft.database.serializers.LocationSerializer;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import com.rafaelsms.potocraft.database.serializers.SimpleSerializer;
import com.rafaelsms.potocraft.plugin.player.BaseProfile;
import com.rafaelsms.potocraft.util.TickableTask;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.database.HomeSerializer;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Profile extends BaseProfile implements TickableTask {

    private final @NotNull TeleporterPlugin plugin;

    private final CachedField<Boolean> acceptingTeleportRequests;
    private final CachedField<Integer> teleportCooldownTicks;

    private final CachedField<Location> backLocation;
    private final CachedField<Location> deathLocation;
    private final CachedMapField<Home> homeMap;

    protected Profile(@NotNull UUID id, @NotNull TeleporterPlugin plugin) throws DatabaseException {
        super(id);
        Serializer<Location> locationSerializer = LocationSerializer.getSerializer(plugin);
        this.plugin = plugin;
        this.acceptingTeleportRequests =
                new CachedField<>("acceptingTeleportRequests", SimpleSerializer.getSerializer(), this, true);
        this.teleportCooldownTicks =
                new CachedField<>("teleportCooldownTicks", SimpleSerializer.getSerializer(), this, 0);
        this.backLocation = new CachedField<>("backLocation", locationSerializer, this);
        this.deathLocation = new CachedField<>("deathLocation", locationSerializer, this);
        this.homeMap = new CachedMapField<>("homeMap", HomeSerializer.getSerializer(plugin), this);
        fetchDatabaseObjectIfPresent();
    }

    @Override
    public @NotNull MongoCollection<Document> getCollection() throws DatabaseException {
        return plugin.getDatabase().getProfileCollection();
    }

    @Override
    protected List<CachedField<?>> getRegisteredFields() {
        return List.of(acceptingTeleportRequests, teleportCooldownTicks, backLocation, deathLocation, homeMap);
    }

    public void setDeathLocation(@NotNull Location location) {
        deathLocation.set(location);
        setBackLocation(location);
    }

    public void setBackLocation(@NotNull Location location) {
        backLocation.set(location);
    }

    public int getTeleportCooldownTicks() {
        return teleportCooldownTicks.getOrDefault().orElse(0);
    }

    public boolean isAcceptingTeleportRequests() {
        return acceptingTeleportRequests.getOrDefault().orElse(true);
    }

    @Override
    public void tick() {
        int cooldownTicks = getTeleportCooldownTicks();
        if (cooldownTicks > 0) {
            teleportCooldownTicks.set(cooldownTicks - 1);
        }
    }
}
