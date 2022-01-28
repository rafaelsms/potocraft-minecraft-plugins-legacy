package com.rafaelsms.potocraft.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.Plugin;
import com.rafaelsms.potocraft.database.pojo.PlayerObject;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class BaseDatabase<P extends PlayerObject> extends Database {

    protected final @NotNull Plugin plugin;
    private final @NotNull Class<P> pClass;

    public BaseDatabase(@NotNull Plugin plugin, @NotNull Class<P> pClass) {
        super(plugin.getConfiguration().getMongoUri(), plugin.getConfiguration().getMongoDatabase());
        this.plugin = plugin;
        this.pClass = pClass;
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) {
        plugin.logger().error("Database exception occurred:", databaseException);
        if (plugin.getConfiguration().isMongoExceptionFatal()) {
            plugin.shutdown();
        }
    }

    protected MongoCollection<P> getPlayerCollection() throws DatabaseException {
        return getCollection(BaseDatabase.Constants.PLAYER_PROFILE_COLLECTION, pClass);
    }

    public Optional<P> getPlayerObject(@NotNull UUID playerId) throws DatabaseException {
        return throwingWrapper(() -> Optional.ofNullable(getPlayerCollection().find(Filters.eq("playerId", playerId))
                                                                              .first()));
    }

    public Optional<P> getPlayerObject(@NotNull ObjectId objectId) throws DatabaseException {
        return throwingWrapper(() -> Optional.ofNullable(getPlayerCollection().find(Filters.eq("_id", objectId))
                                                                              .first()));
    }

    public boolean savePlayerObject(@NotNull P playerObject) {
        return catchingWrapper(() -> {
            getPlayerCollection().replaceOne(Filters.eq("_id", playerObject.getId()), playerObject, UPSERT);
            return true;
        }).isPresent();
    }

    public static final class Constants {

        public static final String PLAYER_PROFILE_COLLECTION = "playerProfiles";

        // Private constructor
        private Constants() {
        }
    }
}
