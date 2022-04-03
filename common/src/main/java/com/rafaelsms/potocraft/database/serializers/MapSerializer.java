package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapSerializer<V> implements Serializer<Map<String, V>> {

    private final @NotNull Serializer<V> objectSerializer;

    private MapSerializer(@NotNull Serializer<V> objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    public static <V> @NotNull MapSerializer<V> getSerializer(@NotNull Serializer<V> objectSerializer) {
        return new MapSerializer<>(objectSerializer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Map<String, V> fromDocument(@Nullable Object object) {
        Map<String, Object> objectMap = Util.convert(object, obj -> (Map<String, Object>) obj);
        if (objectMap == null) {
            return null;
        }
        Map<String, V> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            map.put(entry.getKey(), objectSerializer.fromDocument(entry.getValue()));
        }
        return map;
    }

    @Override
    public @Nullable Object toDocument(@Nullable Map<String, V> object) {
        if (object == null) {
            return null;
        }
        Map<String, Object> objectMap = new LinkedHashMap<>();
        for (Map.Entry<String, V> entry : object.entrySet()) {
            objectMap.put(entry.getKey(), objectSerializer.toDocument(entry.getValue()));
        }
        return objectMap;
    }
}
