package com.rafaelsms.potocraft.database.fields;

import com.rafaelsms.potocraft.database.CollectionProvider;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.serializers.ListSerializer;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class CachedListField<V> extends CachedField<List<V>> implements List<V> {

    public CachedListField(@NotNull String key,
                           @NotNull Serializer<V> objectSerializer,
                           @NotNull CollectionProvider provider,
                           @Nullable List<V> defaultValue) {
        super(key, ListSerializer.getSerializer(objectSerializer), provider, defaultValue);
    }

    public CachedListField(@NotNull String key,
                           @NotNull Serializer<V> objectSerializer,
                           @NotNull CollectionProvider provider) {
        super(key, ListSerializer.getSerializer(objectSerializer), provider);
    }

    @Override
    public @NotNull Optional<List<V>> get() throws DatabaseException {
        Optional<List<V>> list = super.get();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(list.get()));
    }

    @Override
    public @NotNull Optional<List<V>> getIfFetched() {
        Optional<List<V>> list = super.getIfFetched();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(list.get()));
    }

    private List<V> getValueOrEmpty() {
        return getIfFetched().orElse(List.of());
    }

    private LinkedList<V> getWritableCopy() {
        return getIfFetched().map(LinkedList::new).orElseGet(LinkedList::new);
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
    public boolean contains(Object o) {
        return getValueOrEmpty().contains(o);
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        LinkedList<V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        LinkedList<V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        LinkedList<V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.toArray(a);
    }

    @Override
    public boolean add(V v) {
        LinkedList<V> writableCopy = getWritableCopy();
        boolean add = writableCopy.add(v);
        set(writableCopy);
        return add;
    }

    @Override
    public boolean remove(Object o) {
        LinkedList<V> writableCopy = getWritableCopy();
        boolean remove = writableCopy.remove(o);
        set(writableCopy);
        return remove;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return getValueOrEmpty().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends V> c) {
        LinkedList<V> writableCopy = getWritableCopy();
        boolean addAll = writableCopy.addAll(c);
        set(writableCopy);
        return addAll;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends V> c) {
        LinkedList<V> writableCopy = getWritableCopy();
        boolean addAll = writableCopy.addAll(index, c);
        set(writableCopy);
        return addAll;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        LinkedList<V> writableCopy = getWritableCopy();
        boolean removeAll = writableCopy.removeAll(c);
        set(writableCopy);
        return removeAll;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        LinkedList<V> writableCopy = getWritableCopy();
        boolean retainAll = writableCopy.retainAll(c);
        set(writableCopy);
        return retainAll;
    }

    @Override
    public void clear() {
        set(List.of());
    }

    @Override
    public V get(int index) {
        return getValueOrEmpty().get(index);
    }

    @Override
    public V set(int index, V element) {
        LinkedList<V> writableCopy = getWritableCopy();
        V set = writableCopy.set(index, element);
        set(writableCopy);
        return set;
    }

    @Override
    public void add(int index, V element) {
        LinkedList<V> writableCopy = getWritableCopy();
        writableCopy.add(index, element);
        set(writableCopy);
    }

    @Override
    public V remove(int index) {
        LinkedList<V> writableCopy = getWritableCopy();
        V remove = writableCopy.remove(index);
        set(writableCopy);
        return remove;
    }

    @Override
    public int indexOf(Object o) {
        return getValueOrEmpty().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getValueOrEmpty().lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<V> listIterator() {
        LinkedList<V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<V> listIterator(int index) {
        LinkedList<V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.listIterator(index);
    }

    @NotNull
    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        LinkedList<V> writableCopy = getWritableCopy();
        set(writableCopy);
        return writableCopy.subList(fromIndex, toIndex);
    }
}
