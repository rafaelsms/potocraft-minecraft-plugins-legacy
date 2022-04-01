package com.rafaelsms.potocraft.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SimpleField<T> extends CachedField<T> {

    public SimpleField(@Nullable String namespace,
                       @NotNull String key,
                       @NotNull CollectionProvider provider,
                       @Nullable T defaultValue) {
        super(namespace, key, provider, defaultValue);
    }

    public SimpleField(@Nullable String namespace, @NotNull CollectionProvider provider, @NotNull String key) {
        super(namespace, key, provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @Nullable T parseFromDocument(@Nullable Object databaseObject, @Nullable T defaultValue) {
        return (T) Objects.requireNonNullElse(databaseObject, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V> @Nullable V parseToDocument(@Nullable T value) {
        return (V) value;
    }
}
