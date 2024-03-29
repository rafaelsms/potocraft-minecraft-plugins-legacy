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
    public interface DatabaseOperationSupplier<T> {

        T executeOperation() throws DatabaseException, MongoException;

    }

    /**
     * Runnable that may throw a database exception for us to catch.
     */
    public interface DatabaseOperationRunnable {

        void executeOperation() throws DatabaseException, MongoException;

    }

}
