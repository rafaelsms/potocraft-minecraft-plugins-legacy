package com.rafaelsms.potocraft.database;

import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class FinalUUIDField extends UncachedField<UUID> {

    private final UUID identifier;

    public FinalUUIDField(@NotNull String key, @NotNull UUID identifier) {
        super(key);
        this.identifier = identifier;
    }

    @Override
    public @NotNull Optional<UUID> get() {
        return Optional.of(identifier);
    }

    @Override
    public void set(@Nullable UUID value) {
        throw new NotImplementedException("This method isn't required for UUID fields.");
    }

    @Override
    protected @NotNull CollectionProvider getCollectionProvider() {
        throw new NotImplementedException("This method isn't required for UUID fields.");
    }

    @Override
    protected @Nullable UUID parseFromDocument(@Nullable Object databaseObject) {
        return Optional.ofNullable(databaseObject).map(obj -> (String) obj).map(UUID::fromString).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V> @Nullable V parseToDocument(@Nullable UUID value) {
        return (V) Optional.ofNullable(value).map(UUID::toString).orElse(null);
    }
}
