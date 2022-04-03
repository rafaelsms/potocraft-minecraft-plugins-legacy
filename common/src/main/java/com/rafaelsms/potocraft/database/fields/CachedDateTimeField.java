package com.rafaelsms.potocraft.database.fields;

import com.rafaelsms.potocraft.database.CollectionProvider;
import com.rafaelsms.potocraft.database.serializers.DateTimeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public class CachedDateTimeField extends CachedField<ZonedDateTime> {

    public CachedDateTimeField(@NotNull String key,
                               @NotNull CollectionProvider provider,
                               @Nullable ZonedDateTime defaultValue) {
        super(key, DateTimeSerializer.getSerializer(), provider, defaultValue);
    }

    public CachedDateTimeField(@NotNull String key, @NotNull CollectionProvider provider) {
        super(key, DateTimeSerializer.getSerializer(), provider);
    }
}
