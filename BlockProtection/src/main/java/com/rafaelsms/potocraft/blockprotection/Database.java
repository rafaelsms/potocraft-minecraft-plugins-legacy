package com.rafaelsms.potocraft.blockprotection;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public class Database extends BaseDatabase {

    private final @NotNull BlockProtectionPlugin plugin;

    public Database(@NotNull BlockProtectionPlugin plugin) {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabaseName());
        this.plugin = plugin;
    }

    public MongoCollection<Document> getPlayerCollection() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getMongoPlayerCollection());
    }

    public @NotNull String getProfileNamespace() {
        return plugin.getConfiguration().getMongoPlayerNamespace();
    }
}
