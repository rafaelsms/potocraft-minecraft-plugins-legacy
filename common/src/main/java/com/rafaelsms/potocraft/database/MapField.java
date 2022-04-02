package com.rafaelsms.potocraft.database;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This field will store a map internally that can be read using {@link this#get()} but cannot be changed.
 * <p>
 * To change this map, use its {@link Map's} methods, but won't fetch the database if the value is not cached, resulting
 * in data loss through overwriting.
 *
 * @param <V> type of values used. If not readable/writable directly from {@link Document}, it will be necessary to
 *            extend this class and reimplementing {@link this#parseFromDocument(Object, Map)}.
 */
public class MapField<V> extends CachedField<Map<String, V>> implements Map<String, V> {

    public MapField(@Nullable String namespace,
                    @NotNull String key,
                    @NotNull CollectionProvider provider,
                    @Nullable Map<String, V> defaultValue) {
        super(namespace, key, provider, defaultValue);
    }

    public MapField(@Nullable String namespace, @NotNull String key, @NotNull CollectionProvider provider) {
        super(namespace, key, provider);
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

    @SuppressWarnings("unchecked")
    @Override
    protected @Nullable Map<String, V> parseFromDocument(@Nullable Object databaseObject,
                                                         @Nullable Map<String, V> defaultValue) {
        return Optional.ofNullable(databaseObject).map(obj -> (Map<String, V>) obj).orElse(defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <R> @Nullable R parseToDocument(@Nullable Map<String, V> value) {
        if (value == null) {
            return null;
        }
        Document document = new Document();
        document.putAll(value);
        return (R) document;
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
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        set(Map.of());
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return getValueOrEmpty().keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return getValueOrEmpty().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, V>> entrySet() {
        return getValueOrEmpty().entrySet();
    }
}
