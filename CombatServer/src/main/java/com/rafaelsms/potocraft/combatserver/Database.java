package com.rafaelsms.potocraft.combatserver;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.combatserver.player.Profile;
import com.rafaelsms.potocraft.combatserver.util.InventoryContent;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class Database extends com.rafaelsms.potocraft.database.Database {

    private final @NotNull CombatServerPlugin plugin;

    protected Database(@NotNull CombatServerPlugin plugin) {
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
        return getDatabase().getCollection(plugin.getConfiguration().getMongoPlayerCollectionName());
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

    private MongoCollection<Document> getKitCollection() throws DatabaseException {
        return getDatabase().getCollection(plugin.getConfiguration().getMongoKitCollectionName());
    }

    public Optional<InventoryContent> getKit(@NotNull String kitName) throws DatabaseException {
        return throwingWrapper(() -> {
            Document document = getKitCollection().find(InventoryContent.filterByName(kitName)).first();
            if (document == null) {
                return Optional.empty();
            }
            return Optional.of(new InventoryContent(document));
        });
    }

    public void saveContent(@NotNull InventoryContent inventoryContent) {
        catchingWrapper(() -> getKitCollection().replaceOne(inventoryContent.filterByName(),
                                                            inventoryContent.toDocument(),
                                                            UPSERT));
    }
}
