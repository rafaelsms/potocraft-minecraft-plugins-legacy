package com.rafaelsms.teleporter;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public class Database extends BaseDatabase {

    private final @NotNull TeleporterPlugin plugin;

    protected Database(@NotNull TeleporterPlugin plugin) {
        super(plugin.getConfiguration());
        this.plugin = plugin;
    }

    public @NotNull MongoCollection<Document> getWarpCollection() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getWarpCollection());
    }
}
