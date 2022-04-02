package com.rafaelsms.potocraft.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Float isn't available on MongoDB, so we store as text and parse it back
public class FloatField extends CachedField<Float> {

    public FloatField(@Nullable String namespace,
                      @NotNull String key,
                      @NotNull CollectionProvider provider,
                      @Nullable Float defaultValue) {
        super(namespace, key, provider, defaultValue);
    }

    public FloatField(@Nullable String namespace, @NotNull String key, @NotNull CollectionProvider provider) {
        super(namespace, key, provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V> @Nullable V parseToDocument(@Nullable Float value) {
        return (V) Optional.ofNullable(value).map(String::valueOf).orElse(null);
    }

    @Override
    protected @Nullable Float parseFromDocument(@Nullable Object databaseObject, @Nullable Float defaultValue) {
        return Optional.ofNullable(databaseObject).map(obj -> (String) obj).map(Float::valueOf).orElse(null);
    }
}
