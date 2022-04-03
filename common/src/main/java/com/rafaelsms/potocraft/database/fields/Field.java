package com.rafaelsms.potocraft.database.fields;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface Field<T> {

    @NotNull String getKey();

    @NotNull Serializer<T> getSerializer();

    @NotNull Optional<T> get() throws DatabaseException;

    void set(@Nullable T value) throws DatabaseException;

}
