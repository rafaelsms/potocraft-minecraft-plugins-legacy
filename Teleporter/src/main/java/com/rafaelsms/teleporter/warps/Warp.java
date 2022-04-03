package com.rafaelsms.teleporter.warps;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.IdentifiedDatabaseObject;
import com.rafaelsms.potocraft.database.fields.CachedField;
import com.rafaelsms.potocraft.database.serializers.LocationSerializer;
import com.rafaelsms.potocraft.database.serializers.SimpleSerializer;
import com.rafaelsms.teleporter.TeleporterPlugin;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Warp extends IdentifiedDatabaseObject<String> {

    private final @NotNull TeleporterPlugin plugin;

    private final CachedField<Location> warpLocation;

    public Warp(@NotNull TeleporterPlugin plugin, @NotNull String warpName) {
        super(SimpleSerializer.getSerializer(), warpName);
        this.plugin = plugin;
        this.warpLocation = new CachedField<>("location", LocationSerializer.getSerializer(plugin), this);
    }

    @Override
    public @NotNull MongoCollection<Document> getCollection() throws DatabaseException {
        return plugin.getDatabase().getWarpCollection();
    }

    @Override
    protected List<CachedField<?>> getRegisteredFields() {
        return List.of(warpLocation);
    }
}
