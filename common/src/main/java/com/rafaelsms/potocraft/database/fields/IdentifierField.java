package com.rafaelsms.potocraft.database.fields;

import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IdentifierField<T> implements Field<T> {

    private final @NotNull Serializer<T> serializer;
    private final @NotNull T identifier;

    public IdentifierField(@NotNull Serializer<T> serializer, @NotNull T identifier) {
        this.serializer = serializer;
        this.identifier = identifier;
    }

    @Override
    public @NotNull Optional<T> get() {
        return Optional.of(identifier);
    }

    @Override
    public void set(@Nullable T value) {
        throw new NotImplementedException("This method isn't supported by identifier fields.");
    }

    @Override
    public @NotNull String getKey() {
        return "_id";
    }

    @Override
    public @NotNull Serializer<T> getSerializer() {
        return serializer;
    }
}
