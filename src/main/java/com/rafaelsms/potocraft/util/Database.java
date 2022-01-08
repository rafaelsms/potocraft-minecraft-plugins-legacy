package com.rafaelsms.potocraft.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rafaelsms.potocraft.Plugin;
import com.rafaelsms.potocraft.user.UserProfile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Database {

    protected final @NotNull Plugin plugin;

    private @Nullable MongoClient mongoClient = null;

    public Database(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    private MongoCollection<Document> getUserProfiles() {
        return getDatabase().getCollection(Constants.USER_PROFILE_COLLECTION);
    }

    protected <T extends UserProfile> void retrieveProfile(@NotNull UUID playerUniqueId,
                                                           @NotNull Function<Document, T> profileFactory,
                                                           @Nullable Consumer<T> profileConsumer,
                                                           @Nullable Runnable nullProfileRunnable,
                                                           @Nullable Consumer<Exception> exceptionConsumer) {
        try {
            Optional<T> profile = getProfile(playerUniqueId, profileFactory);
            if (profile.isPresent()) {
                if (profileConsumer == null) return;
                profileConsumer.accept(profile.get());
            } else {
                if (nullProfileRunnable == null) return;
                nullProfileRunnable.run();
            }
        } catch (Exception exception) {
            if (exceptionConsumer == null) return;
            exceptionConsumer.accept(exception);
        }
    }

    private <T extends UserProfile> Optional<T> getProfile(
            @NotNull UUID playerId, @NotNull Function<Document, T> profileFactory) throws DatabaseException {
        return handleExceptions(() -> {
            Document profileDocument = getUserProfiles().find(UserProfile.filterId(playerId)).first();
            if (profileDocument != null) {
                return profileFactory.apply(profileDocument);
            }
            return null;
        });
    }

    protected void saveProfile(@NotNull UserProfile userProfile,
                               @Nullable Runnable successfulSaveRunnable,
                               @Nullable Consumer<Exception> exceptionConsumer) {
        try {
            Optional<Boolean> optionalBoolean = saveProfile(userProfile);
            if (optionalBoolean.isEmpty() || !optionalBoolean.get())
                throw new DatabaseException("Failed to save profile");
            if (successfulSaveRunnable == null) return;
            successfulSaveRunnable.run();
        } catch (DatabaseException exception) {
            if (exceptionConsumer == null) return;
            exceptionConsumer.accept(exception);
        }
    }

    protected Optional<Boolean> saveProfile(@NotNull UserProfile userProfile) throws DatabaseException {
        return handleExceptions(
                () -> getUserProfiles().replaceOne(userProfile.filterId(), userProfile.toDocument()).wasAcknowledged());
    }

    protected <T> Optional<T> handleExceptions(@NotNull Supplier<T> supplier) throws DatabaseException {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception exception) {
            plugin.logger().error("Database exception: %s".formatted(exception.getLocalizedMessage()));
            if (plugin.getSettings().isMongoFailPrintable()) {
                exception.printStackTrace();
            }
            if (plugin.getSettings().isMongoFailFatal()) {
                plugin.getCommonServer().shutdown();
            }
            throw new DatabaseException(exception);
        }
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

}
