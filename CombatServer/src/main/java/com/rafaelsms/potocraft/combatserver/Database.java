package com.rafaelsms.potocraft.combatserver;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public class Database extends BaseDatabase {

    private final @NotNull CombatServerPlugin plugin;

    protected Database(@NotNull CombatServerPlugin plugin) {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabaseName());
        this.plugin = plugin;
    }

    public @NotNull MongoCollection<Document> getProfileCollection() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getMongoPlayerCollectionName());
    }
}
