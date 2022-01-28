package com.rafaelsms.potocraft.database.converters;

import com.rafaelsms.potocraft.database.DatabaseObjectManager;
import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateConverter implements DatabaseObjectManager.Converter<ZonedDateTime> {
    @Override
    public Object parseToDocument(@Nullable ZonedDateTime object) {
        return Util.convert(object, dateTime -> dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    @Override
    public @Nullable ZonedDateTime fromDocument(@Nullable Object object) {
        return Util.convert((CharSequence) object,
                            string -> ZonedDateTime.parse(string, DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }
}
