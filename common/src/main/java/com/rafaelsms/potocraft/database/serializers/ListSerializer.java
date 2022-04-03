package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class ListSerializer<V> implements Serializer<List<V>> {

    private final @NotNull Serializer<V> objectSerializer;

    private ListSerializer(@NotNull Serializer<V> objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    public static <V> @NotNull ListSerializer<V> getSerializer(@NotNull Serializer<V> objectSerializer) {
        return new ListSerializer<>(objectSerializer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable List<V> fromDocument(@Nullable Object object) {
        List<Object> objectList = Util.convert(object, obj -> (List<Object>) obj);
        if (objectList == null) {
            return null;
        }
        List<V> list = new LinkedList<>();
        for (Object obj : objectList) {
            list.add(objectSerializer.fromDocument(obj));
        }
        return list;
    }

    @Override
    public @Nullable Object toDocument(@Nullable List<V> object) {
        if (object == null) {
            return null;
        }
        List<Object> objectList = new LinkedList<>();
        for (V v : object) {
            objectList.add(objectSerializer.toDocument(v));
        }
        return objectList;
    }
}
