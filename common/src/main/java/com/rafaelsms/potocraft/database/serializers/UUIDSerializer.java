package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDSerializer implements Serializer<UUID> {

    private static final Serializer<UUID> SERIALIZER = new UUIDSerializer();

    public static @NotNull Serializer<UUID> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @Nullable UUID fromDocument(@Nullable Object object) {
        return Util.convert(Util.convert(object, String.class), UUID::fromString);
    }

    @Override
    public @Nullable Object toDocument(@Nullable UUID object) {
        return Util.convert(object, UUID::toString);
    }
}
