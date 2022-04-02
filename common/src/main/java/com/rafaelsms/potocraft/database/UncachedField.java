package com.rafaelsms.potocraft.database;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class UncachedField<T> {

    private final @NotNull String key;

    public UncachedField(@NotNull String key) {
        this.key = key;
    }

    public @NotNull String getKey() {
        return key;
    }

    public @NotNull Bson getFieldProjection() {
        return Projections.include(getKey());
    }

    public @NotNull Optional<T> get() throws DatabaseException {
        try {
            CollectionProvider provider = getCollectionProvider();
            Object fromDatabase = Optional.ofNullable(provider.getCollection()
                                                              .find(provider.getIdQuery())
                                                              .projection(getFieldProjection())
                                                              .first()).map(document -> document.get(key)).orElse(null);
            return Optional.ofNullable(parseFromDocument(fromDatabase));
        } catch (IllegalStateException exception) {
            // This way we can do something about missing fields where it is critical
            throw new DatabaseException(exception);
        }
    }

    public void set(@Nullable T value) throws DatabaseException {
        CollectionProvider provider = getCollectionProvider();
        provider.getCollection().updateOne(provider.getIdQuery(), Updates.set(getKey(), parseToDocument(value)));
    }

    protected abstract @NotNull CollectionProvider getCollectionProvider();

    protected abstract @Nullable T parseFromDocument(@Nullable Object databaseObject);

    protected abstract <V> @Nullable V parseToDocument(@Nullable T value);
}
