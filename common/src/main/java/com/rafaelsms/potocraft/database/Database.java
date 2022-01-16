package com.rafaelsms.potocraft.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class Database {

    protected static final @NotNull ReplaceOptions UPSERT = new ReplaceOptions().upsert(true);

    private @Nullable MongoClient mongoClient = null;
    private final @NotNull String uri;
    private final @NotNull String databaseName;

    protected Database(@NotNull String uri, @NotNull String databaseName) {
        this.uri = uri;
        this.databaseName = databaseName;
    }

    /**
     * Handles the database exception thrown executing a function that may be always called on database exceptions.
     *
     * @param databaseException database exception thrown
     * @throws DatabaseException in case the user needs to do a database operation
     * @see #throwingWrapper(DatabaseOperation) to catch and handle the exception while still being able to tell if
     * database threw
     * @see #catchingWrapper(DatabaseOperation) to ignore the exception and receiving null instead of failing
     */
    protected abstract void handleException(@NotNull DatabaseException databaseException) throws DatabaseException;

    /**
     * Wrapper to a database operation that may return a value. The wrapper throws any database related exception.
     *
     * @param supplier database operation that returns a value
     * @param <T>      type of value expected from the database
     * @return returned value from supplier if succeeded
     * @throws DatabaseException if database operation failed
     * @see #handleException(DatabaseException) executed method in case of a exception
     */
    protected <T> T throwingWrapper(@NotNull DatabaseOperation<T> supplier) throws DatabaseException {
        try {
            return supplier.executeOperation();
        } catch (MongoException | DatabaseException exception) {
            DatabaseException databaseException = new DatabaseException(exception);
            handleException(databaseException);
            throw databaseException;
        }
    }

    /**
     * Wrapper to a database operation that may return a value. The wrapper catches any database related exception and
     * returns an empty optional in this case.
     *
     * @param supplier database operation that returns a value
     * @param <T>      type of value expected from the database
     * @return an optional to the database value, it'll be empty if the database failed.
     */
    protected <T> Optional<T> catchingWrapper(@NotNull DatabaseOperation<T> supplier) {
        try {
            return Optional.ofNullable(supplier.executeOperation());
        } catch (MongoException | DatabaseException exception) {
            return Optional.empty();
        }
    }

    /**
     * @return a mongo database instance if succeeded
     * @throws DatabaseException if connection failed
     * @see #UPSERT useful {@link ReplaceOptions} replace option
     */
    protected MongoDatabase getDatabase() throws DatabaseException {
        try {
            if (mongoClient == null) {
                mongoClient = new MongoClient(new MongoClientURI(uri));
            }
            return mongoClient.getDatabase(databaseName);
        } catch (MongoException exception) {
            throw new DatabaseException(exception);
        }
    }

    /**
     * Closes internal {@link MongoClient} instance if it exists.
     */
    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
            this.mongoClient = null;
        }
    }

    /**
     * Supplier that may throw a database exception for us to catch.
     *
     * @param <T> return type of the operation
     */
    public interface DatabaseOperation<T> {

        T executeOperation() throws DatabaseException, MongoException;

    }

    public static class DatabaseException extends Exception {

        public DatabaseException(Exception exception) {
            super(exception);
        }

    }

    /*public Database(@NotNull Plugin<?, ?, ?> plugin) {
        this.plugin = plugin;
        try {
            getUserProfiles().createIndex(Indexes.text(Profile.lastNameField()), new IndexOptions().background(false));
        } catch (DatabaseException exception) {
            plugin.logger().warn("Failed to create text index for player names!");
            exception.printStackTrace();
        }
    }

    private MongoCollection<Document> getUserProfiles() throws DatabaseException {
        return getDatabase().getCollection(Constants.USER_PROFILE_COLLECTION);
    }

    protected abstract @NotNull T makeProfile(@NotNull Document document);

    protected Optional<T> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return handleException(() -> {
            Document document = getUserProfiles().find(Profile.filterId(playerId)).first();
            if (document == null) {
                return Optional.empty();
            }
            return Optional.of(makeProfile(document));
        });
    }

    protected List<T> searchOfflineProfile(@NotNull String namePattern) throws DatabaseException {
        return handleException(() -> {
            List<T> profiles = new ArrayList<>();
            getUserProfiles().find(Filters.regex(Profile.lastNameField(), namePattern, "i"))
                    //.projection(Projections.metaTextScore("textScore"))
                    //.sort(Sorts.metaTextScore("textScore"))
                    .limit(6).forEach((Consumer<? super Document>) document -> profiles.add(makeProfile(document)));
            return profiles;
        });
    }

    protected void saveProfile(T profile) throws DatabaseException {
        handleException(() -> {
            getUserProfiles().replaceOne(profile.filterId(), profile.toDocument(), UPSERT);
            return null;
        });
    }*/
}
