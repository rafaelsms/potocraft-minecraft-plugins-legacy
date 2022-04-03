package com.rafaelsms.teleporter.player;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.fields.CachedField;
import com.rafaelsms.potocraft.database.fields.CachedMapField;
import com.rafaelsms.potocraft.database.serializers.LocationSerializer;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import com.rafaelsms.potocraft.database.serializers.SimpleSerializer;
import com.rafaelsms.potocraft.player.BaseProfile;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.database.HomeSerializer;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Profile extends BaseProfile {

    private final @NotNull TeleporterPlugin plugin;

    private final CachedField<Boolean> acceptingTeleportRequests;
    private final CachedField<Integer> teleportCooldownTicks;

    private final CachedField<Location> backLocation;
    private final CachedField<Location> deathLocation;
    private final CachedMapField<Home> homeMap;

    protected Profile(@NotNull UUID id, @NotNull TeleporterPlugin plugin) {
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
    }

    @Override
    public @NotNull MongoCollection<Document> getCollection() throws DatabaseException {
        return plugin.getDatabase().getProfileCollection();
    }

    @Override
    protected List<CachedField<?>> getRegisteredFields() {
        return List.of(acceptingTeleportRequests, teleportCooldownTicks, backLocation, deathLocation, homeMap);
    }
}
