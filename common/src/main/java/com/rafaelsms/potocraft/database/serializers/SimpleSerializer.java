package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class SimpleSerializer<T> implements Serializer<T> {

    private static final Serializer<Object> SERIALIZER = new SimpleSerializer<>();

    public static <R> @NotNull Serializer<R> getSerializer() {
        return (Serializer<R>) SERIALIZER;
    }

    @Override
    public @Nullable T fromDocument(@Nullable Object object) {
        return Util.convert(object, obj -> (T) obj);
    }

    @Override
    public @Nullable Object toDocument(@Nullable T object) {
        return object;
    }
}
