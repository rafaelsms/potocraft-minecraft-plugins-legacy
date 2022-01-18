package com.rafaelsms.potocraft.hardcore;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.hardcore.players.Profile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class Database extends com.rafaelsms.potocraft.database.Database {

    private final @NotNull HardcorePlugin plugin;

    public Database(@NotNull HardcorePlugin plugin) {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabaseName());
        this.plugin = plugin;
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) throws DatabaseException {
        plugin.logger().warn("Database exception:", databaseException);
        if (plugin.getConfiguration().isMongoDatabaseExceptionFatal()) {
            plugin.getServer().shutdown();
        }
        throw databaseException;
    }

    private MongoCollection<Document> getHardcoreProfileCollection() throws DatabaseException {
        return getDatabase().getCollection(plugin.getConfiguration().getMongoProfileCollectionName());
    }

    /**
     * Retrieve a player profile from the database or creates one if profile doesn't exist.
     *
     * @param playerId player's unique identifier
     * @return an optional, empty if database failure
     */
    public @NotNull Optional<Profile> getPlayerProfile(@NotNull UUID playerId) {
        return catchingWrapper(() -> {
            Document document = getHardcoreProfileCollection().find(Profile.fromId(playerId)).first();
            if (document == null) {
                return new Profile(playerId);
            }
            return new Profile(document);
        });
    }

    public void savePlayerProfile(@NotNull Profile profile) throws DatabaseException {
        throwingWrapper(() -> {
            getHardcoreProfileCollection().replaceOne(profile.fromId(), profile.toDocument(), UPSERT);
            return null;
        });
    }
}
