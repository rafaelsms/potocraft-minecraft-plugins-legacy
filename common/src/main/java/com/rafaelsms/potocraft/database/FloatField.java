package com.rafaelsms.potocraft.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Float isn't available on MongoDB, so we store as text and parse it back
public class FloatField extends UncachedField<Float> {

    public FloatField(@NotNull String key, @Nullable Float defaultValue) {
        super(, key, defaultValue);
    }

    public FloatField(@NotNull String key) {
        super(key);
    }

    @Override
    protected @Nullable Float parseFromDatabase(@Nullable Object databaseObject, @Nullable Float defaultValue) {
        return Optional.ofNullable(databaseObject).map(obj -> (String) obj).map(Float::valueOf).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V> @Nullable V parseToDocument(@Nullable Float value) {
        return (V) Optional.ofNullable(value).map(String::valueOf).orElse(null);
    }
}
