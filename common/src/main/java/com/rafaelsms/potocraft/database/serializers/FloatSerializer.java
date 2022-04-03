package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatSerializer implements Serializer<Float> {

    private static final Serializer<Float> SERIALIZER = new FloatSerializer();

    public static @NotNull Serializer<Float> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @Nullable Float fromDocument(@Nullable Object object) {
        return Util.convert(Util.convert(object, String.class), Float::valueOf);
    }

    @Override
    public @Nullable Object toDocument(@Nullable Float object) {
        return Util.convert(object, String::valueOf);
    }
}
