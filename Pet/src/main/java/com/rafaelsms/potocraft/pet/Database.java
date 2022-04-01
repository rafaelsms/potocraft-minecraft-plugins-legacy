package com.rafaelsms.potocraft.pet;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.pet.player.Profile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class Database extends BaseDatabase {

    private final @NotNull PetPlugin plugin;

    protected Database(@NotNull PetPlugin plugin) {
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

    private MongoCollection<Document> getProfileCollection() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getMongoPlayerCollectionName());
    }

    public Optional<Profile> getProfile(UUID playerId) throws DatabaseException {
        return throwingWrapper(() -> {
            Document document = getProfileCollection().find(Profile.filterById(playerId)).first();
            if (document == null) {
                return Optional.empty();
            }
            return Optional.of(new Profile(document));
        });
    }

    public void saveProfile(@NotNull Profile profile) {
        catchingWrapper(() -> getProfileCollection().replaceOne(profile.filterById(), profile.toDocument(), UPSERT));
    }
}
