package com.rafaelsms.potocraft.common.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.rafaelsms.potocraft.common.Plugin;
import com.rafaelsms.potocraft.common.profile.Profile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class Database<T extends Profile> {

    private static final ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);

    protected final @NotNull Plugin plugin;

    private @Nullable MongoClient mongoClient = null;

    public Database(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    private MongoCollection<Document> getUserProfiles() throws DatabaseException {
        return getDatabase().getCollection(Constants.USER_PROFILE_COLLECTION);
    }

    protected abstract @NotNull T makeProfile(@NotNull Document document);

    protected Optional<T> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return handleException(() -> {
            Document document = getUserProfiles().find(Profile.filterId(playerId)).first();
            if (document == null) return Optional.empty();
            return Optional.of(makeProfile(document));
        });
    }

    protected void saveProfile(T profile) throws DatabaseException {
        handleException(() -> {
            long modifiedCount = getUserProfiles()
                    .replaceOne(profile.filterId(), profile.toDocument(), replaceOptions)
                    .getModifiedCount();
            if (modifiedCount != 1) {
                throw new DatabaseException("Expected only 1 database change, got %d".formatted(modifiedCount));
            }
            return null;
        });
    }

    protected <U> U handleException(@NotNull DatabaseOperation<U> supplier) throws DatabaseException {
        try {
            return supplier.executeOperation();
        } catch (Exception exception) {
            if (plugin.getSettings().isMongoFailPrintable()) {
                exception.printStackTrace();
            }
            if (plugin.getSettings().isMongoFailFatal()) {
                plugin.getCommonServer().shutdown();
            }
            throw new DatabaseException(exception);
        }
    }

    private MongoDatabase getDatabase() throws DatabaseException {
        try {
            if (mongoClient == null) {
                mongoClient = new MongoClient(new MongoClientURI(plugin.getSettings().getMongoURI()));
            }
            return mongoClient.getDatabase(plugin.getSettings().getMongoDatabase());
        } catch (Exception exception) {
            throw new DatabaseException(exception);
        }
    }

    protected static final class Constants {

        public static final String USER_PROFILE_COLLECTION = "userProfiles";

    }

    private interface DatabaseOperation<T> {
        T executeOperation() throws DatabaseException;
    }
}
