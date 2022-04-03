package com.rafaelsms.potocraft.database.fields;

import com.rafaelsms.potocraft.database.CollectionProvider;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.serializers.MapSerializer;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CachedMapField<V> extends CachedField<Map<String, V>> implements Map<String, V> {

    public CachedMapField(@NotNull String key,
                          @NotNull Serializer<V> objectSerializer,
                          @NotNull CollectionProvider provider,
                          @Nullable Map<String, V> defaultValue) {
        super(key, MapSerializer.getSerializer(objectSerializer), provider, defaultValue);
    }

    public CachedMapField(@NotNull String key,
                          @NotNull Serializer<V> objectSerializer,
                          @NotNull CollectionProvider provider) {
        super(key, MapSerializer.getSerializer(objectSerializer), provider);
    }

    @Override
    public @NotNull Optional<Map<String, V>> get() throws DatabaseException {
        Optional<Map<String, V>> map = super.get();
        if (map.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(map.get()));
    }

    @Override
    public @NotNull Optional<Map<String, V>> getIfFetched() {
        Optional<Map<String, V>> map = super.getIfFetched();
        if (map.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(map.get()));
    }

    private Map<String, V> getValueOrEmpty() {
        return getIfFetched().orElse(Map.of());
    }

    private LinkedHashMap<String, V> getWritableCopy() {
        return getIfFetched().map(LinkedHashMap::new).orElseGet(LinkedHashMap::new);
    }

    @Override
    public int size() {
        return getValueOrEmpty().size();
    }

    @Override
    public boolean isEmpty() {
        return getValueOrEmpty().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getValueOrEmpty().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getValueOrEmpty().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return getValueOrEmpty().get(key);
    }

    @Override
    public @Nullable V put(@NotNull String key, @Nullable V value) {
        Map<String, V> hashMap = getWritableCopy();
        V existingItem = hashMap.put(key, value);
        set(hashMap);
        return existingItem;
    }

    @Override
    public @Nullable V remove(@NotNull Object key) {
        Map<String, V> hashMap = getWritableCopy();
        V removedItem = hashMap.remove(key);
        set(hashMap);
        return removedItem;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends V> m) {
        LinkedHashMap<String, V> writableCopy = getWritableCopy();
        writableCopy.putAll(m);
        set(writableCopy);
    }

    @Override
    public void clear() {
        set(Map.of());
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        LinkedHashMap<String, V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        LinkedHashMap<String, V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, V>> entrySet() {
        LinkedHashMap<String, V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.entrySet();
    }
}
