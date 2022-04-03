package com.rafaelsms.potocraft.database.serializers;

import org.jetbrains.annotations.Nullable;

public interface Serializer<T> {

    @Nullable T fromDocument(@Nullable Object object);

    @Nullable Object toDocument(@Nullable T object);

}
