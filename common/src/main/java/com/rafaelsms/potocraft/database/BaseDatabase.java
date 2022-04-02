package com.rafaelsms.potocraft.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

public abstract class BaseDatabase implements Closeable {

    public static final @NotNull UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    private @Nullable MongoClient mongoClient = null;
    private final @NotNull String uri;
    private final @NotNull String databaseName;

    protected BaseDatabase(@NotNull String uri, @NotNull String databaseName) {
        this.uri = uri;
        this.databaseName = databaseName;
    }

    /**
     * @return a mongo database instance if succeeded
     * @throws DatabaseException if connection failed
     * @see #UPSERT useful {@link ReplaceOptions} replace option
     */
    protected @NotNull MongoDatabase getClient() throws DatabaseException {
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
    @Override
    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
            this.mongoClient = null;
        }
    }
}