package com.rafaelsms.potocraft.database.serializers;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeSerializer implements Serializer<ZonedDateTime> {

    private final static Serializer<ZonedDateTime> SERIALIZER = new DateTimeSerializer();

    public static Serializer<ZonedDateTime> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @Nullable ZonedDateTime fromDocument(@Nullable Object object) {
        return Util.convert(Util.convert(object, String.class),
                            str -> ZonedDateTime.parse(str, DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    @Override
    public @Nullable Object toDocument(@Nullable ZonedDateTime object) {
        return Util.convert(object, dateTime -> dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }
}
