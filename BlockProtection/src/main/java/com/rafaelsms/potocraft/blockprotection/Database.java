package com.rafaelsms.potocraft.blockprotection;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.blockprotection.players.Profile;
import com.rafaelsms.potocraft.blockprotection.protection.ProtectedRegion;
import com.rafaelsms.potocraft.blockprotection.util.Box;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class Database extends com.rafaelsms.potocraft.database.Database {

    private final @NotNull BlockProtectionPlugin plugin;

    public Database(@NotNull BlockProtectionPlugin plugin) {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabaseName());
        this.plugin = plugin;
    }

    private MongoCollection<Document> getPlayerCollection() throws DatabaseException {
        return getDatabase().getCollection(plugin.getConfiguration().getMongoPlayerCollection());
    }

    private MongoCollection<Document> getRegionCollection() throws DatabaseException {
        return getDatabase().getCollection(plugin.getConfiguration().getMongoRegionCollection());
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) throws DatabaseException {
        plugin.logger().warn("Database exception:", databaseException);
        if (plugin.getConfiguration().isMongoDatabaseExceptionFatal()) {
            plugin.getServer().shutdown();
        }
        throw databaseException;
    }

    public Optional<Profile> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return throwingWrapper(() -> {
            Document playerProfile = getPlayerCollection().find(Profile.filterId(playerId)).first();
            return Optional.ofNullable(Util.convert(playerProfile, document -> new Profile(plugin, document)));
        });
    }

    public void saveProfileCatching(@NotNull Profile profile) {
        catchingWrapper(() -> {
            getPlayerCollection().replaceOne(profile.filterId(), profile.toDocument(), UPSERT);
        });
    }

    public boolean collidesWith(@NotNull Box box) throws DatabaseException {
        // We wrap around using ProtectedRegion because the box is a document inside the protected region's document
        return throwingWrapper(() -> getRegionCollection().find(ProtectedRegion.filterColliding(box.filterColliding()))
                                                          .first() != null);
    }
}
