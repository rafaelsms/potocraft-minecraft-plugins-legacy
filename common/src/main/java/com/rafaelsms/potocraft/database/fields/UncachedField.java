package com.rafaelsms.potocraft.database.fields;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.rafaelsms.potocraft.database.CollectionProvider;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class UncachedField<T> implements Field<T> {

    private final @NotNull String key;
    private final @NotNull Serializer<T> serializer;
    private final @NotNull CollectionProvider collectionProvider;

    public UncachedField(@NotNull String key,
                         @NotNull Serializer<T> serializer,
                         @NotNull CollectionProvider collectionProvider) {
        this.key = key;
        this.serializer = serializer;
        this.collectionProvider = collectionProvider;
    }

    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    @NotNull
    public Serializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public @NotNull Optional<T> get() throws DatabaseException {
        try {
            CollectionProvider provider = getCollectionProvider();
            Object fromDatabase = Optional.ofNullable(provider.getCollection()
                                                              .find(provider.getIdQuery())
                                                              .projection(getFieldProjection())
                                                              .first()).map(document -> document.get(key)).orElse(null);
            return Optional.ofNullable(getSerializer().fromDocument(fromDatabase));
        } catch (IllegalStateException exception) {
            // This way we can do something about missing fields where it is critical
            throw new DatabaseException(exception);
        }
    }

    @Override
    public void set(@Nullable T value) throws DatabaseException {
        CollectionProvider provider = getCollectionProvider();
        provider.getCollection()
                .updateOne(provider.getIdQuery(), Updates.set(getKey(), getSerializer().toDocument(value)));
    }

    @NotNull
    public Bson getFieldProjection() {
        return Projections.include(getKey());
    }

    protected @NotNull CollectionProvider getCollectionProvider() {
        return collectionProvider;
    }
}
