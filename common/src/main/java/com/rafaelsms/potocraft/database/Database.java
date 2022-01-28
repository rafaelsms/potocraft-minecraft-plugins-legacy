package com.rafaelsms.potocraft.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.rafaelsms.potocraft.database.pojo.BaseObject;
import com.rafaelsms.potocraft.database.pojo.HomeObject;
import com.rafaelsms.potocraft.database.pojo.LocationObject;
import com.rafaelsms.potocraft.database.pojo.PlayerObject;
import com.rafaelsms.potocraft.database.pojo.ProxyProfile;
import com.rafaelsms.potocraft.database.pojo.ReportEntryObject;
import com.rafaelsms.potocraft.database.pojo.ServerProfile;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public abstract class Database {

    protected static final ReplaceOptions UPSERT = new ReplaceOptions().upsert(true);

    private final @NotNull String uri;
    private final @NotNull String databaseName;

    private @Nullable MongoClient mongoClient = null;

    protected Database(@NotNull String uri, @NotNull String databaseName) {
        this.uri = uri;
        this.databaseName = databaseName;
    }

    /**
     * Handles the database exception thrown executing a function that may be always called on database exceptions.
     *
     * @param databaseException database exception thrown
     * @throws DatabaseException in case the user needs to do a database operation
     * @see #throwingWrapper(DatabaseOperationSupplier) to catch and handle the exception while still being able to tell
     * if database threw
     * @see #catchingWrapper(DatabaseOperationSupplier) to ignore the exception and receiving null instead of failing
     */
    protected abstract void handleException(@NotNull DatabaseException databaseException) throws DatabaseException;

    /**
     * Wrapper to a database operation that may return a value. The wrapper throws any database related exception.
     *
     * @param supplier database operation that returns a value
     * @param <T>      type of value expected from the database
     * @return returned value from supplier if succeeded
     * @throws DatabaseException if database operation failed
     * @see #handleException(DatabaseException) executed method in case of an exception
     */
    protected <T> T throwingWrapper(@NotNull Database.DatabaseOperationSupplier<T> supplier) throws DatabaseException {
        try {
            return supplier.executeOperation();
        } catch (MongoException | DatabaseException exception) {
            DatabaseException databaseException = new DatabaseException(exception);
            handleException(databaseException);
            throw databaseException;
        }
    }

    /**
     * Wrapper to a database operation that doesn't return any value. The wrapper throws any database related exceptions
     * after handling them
     *
     * @param runnable database operation to be executed
     * @throws DatabaseException if database operation failed
     */
    protected void throwingWrapper(@NotNull DatabaseOperationRunnable runnable) throws DatabaseException {
        throwingWrapper(() -> {
            runnable.executeOperation();
            return null;
        });
    }

    /**
     * Wrapper to a database operation that may return a value. The wrapper catches any database related exception and
     * returns an empty optional in this case.
     *
     * @param supplier database operation that returns a value
     * @param <T>      type of value expected from the database
     * @return an optional to the database value, it'll be empty if the database failed.
     */
    protected <T> Optional<T> catchingWrapper(@NotNull Database.DatabaseOperationSupplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.executeOperation());
        } catch (MongoException | DatabaseException exception) {
            return Optional.empty();
        }
    }

    /**
     * Wrapper to a database operation that doesn't return any value. The wrapper catches any database-related
     * exceptions.
     *
     * @param runnable database operation to be executed
     */
    protected void catchingWrapper(@NotNull DatabaseOperationRunnable runnable) {
        catchingWrapper(() -> {
            runnable.executeOperation();
            return null;
        });
    }

    private <T> ClassModel<T> withDiscriminator(Class<T> tClass) {
        return ClassModel.builder(tClass).enableDiscriminator(true).build();
    }

    /**
     * @return a mongo database instance if succeeded
     * @throws DatabaseException if connection failed
     */
    private MongoDatabase getDatabase() throws DatabaseException {
        try {
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                                                               .automatic(true)
                                                               .register(withDiscriminator(BaseObject.class),
                                                                         withDiscriminator(ReportEntryObject.class),
                                                                         withDiscriminator(LocationObject.class),
                                                                         withDiscriminator(HomeObject.class),
                                                                         withDiscriminator(PlayerObject.class),
                                                                         withDiscriminator(ServerProfile.class),
                                                                         withDiscriminator(ProxyProfile.class))
                                                               .build();
            CodecRegistry pojoCodecRegistry =
                    fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
            if (mongoClient == null) {
                mongoClient = new MongoClient(new MongoClientURI(uri));
            }
            return mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        } catch (MongoException exception) {
            throw new DatabaseException(exception);
        }
    }

    /**
     * @param collectionName collection's name
     * @param tClass         type's class instance
     * @param <T>            type to convert mongo JSON to
     * @return a mongo database collection converting its class to a
     * @throws DatabaseException if anything failed
     */
    protected <T> MongoCollection<T> getCollection(@NotNull String collectionName, @NotNull Class<T> tClass) throws
                                                                                                             DatabaseException {
        return getDatabase().getCollection(collectionName, tClass);
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
    public interface DatabaseOperationSupplier<T> {

        T executeOperation() throws DatabaseException, MongoException;

    }

    /**
     * Runnable that may throw a database exception for us to catch.
     */
    public interface DatabaseOperationRunnable {

        void executeOperation() throws DatabaseException, MongoException;

    }

    public static class DatabaseException extends Exception {

        public DatabaseException(Exception exception) {
            super(exception);
        }

    }
}
