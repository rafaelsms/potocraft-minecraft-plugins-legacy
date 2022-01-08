package com.rafaelsms.potocraft.common.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.rafaelsms.potocraft.common.Plugin;
import com.rafaelsms.potocraft.common.profile.Profile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class Database {

    protected final @NotNull Plugin plugin;

    private @Nullable MongoClient mongoClient = null;

    public Database(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    private MongoCollection<Document> getUserProfiles() {
        return getDatabase().getCollection(Constants.USER_PROFILE_COLLECTION);
    }

    protected <T extends Profile> CompletableFuture<T> getProfile(
            @NotNull UUID playerId, @NotNull Function<Document, T> profileFactory) {
        return handleExceptions(() -> {
            Document profileDocument = getUserProfiles().find(Profile.filterId(playerId)).first();
            if (profileDocument != null) {
                plugin.debug("Found player profile (uuid = %s)".formatted(playerId.toString()));
                return profileFactory.apply(profileDocument);
            }
            plugin.debug("Failed to find player profile (uuid = %s)".formatted(playerId.toString()));
            return null;
        });
    }

    protected @NotNull CompletableFuture<Void> saveProfile(@NotNull Profile profile) {
        return handleExceptions(() -> {
            long modifiedCount = getUserProfiles().replaceOne(
                    // Allow insert when none found
                    profile.filterId(), profile.toDocument(), new ReplaceOptions().upsert(true)
            ).getModifiedCount();
            if (modifiedCount != 1) {
                throw new DatabaseException("Profile saved replaced %d profiles".formatted(modifiedCount));
            }
            plugin.debug("Saved player profile: %s".formatted(profile.getUniqueId().toString()));
            return null;
        });
    }

    protected <T> @NotNull CompletableFuture<T> handleExceptions(@NotNull DatabaseOperation<T> operation) {
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            future.complete(operation.executeOperation());
        } catch (Exception exception) {
            plugin.logger().error("Database exception: %s".formatted(exception.getLocalizedMessage()));
            if (plugin.getSettings().isMongoFailPrintable()) {
                exception.printStackTrace();
            }
            if (plugin.getSettings().isMongoFailFatal()) {
                plugin.getCommonServer().shutdown();
            }
            future.completeExceptionally(exception);
        }
        return future.orTimeout(plugin.getSettings().getDatabaseTimeout(), TimeUnit.MILLISECONDS);
    }

    protected MongoDatabase getDatabase() throws MongoException {
        try {
            if (mongoClient == null) {
                mongoClient = new MongoClient(new MongoClientURI(plugin.getSettings().getMongoURI()));
            }
            return mongoClient.getDatabase(plugin.getSettings().getMongoDatabase());
        } catch (Exception exception) {
            throw new MongoException(exception.getLocalizedMessage());
        }
    }

    protected static final class Constants {

        public static final String USER_PROFILE_COLLECTION = "userProfiles";

    }

    private interface DatabaseOperation<T> {

        T executeOperation() throws Exception;

    }
}
