package com.rafaelsms.potocraft.util;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class Converter {

    private Converter() {
    }

    public static @Nullable UUID toUUID(@Nullable String string) {
        return string == null ? null : UUID.fromString(string);
    }

    public static @Nullable String fromUUID(@Nullable UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    public static @Nullable ZonedDateTime toDateTime(@Nullable String string) {
        if (string == null) return null;
        return ZonedDateTime.parse(string, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static @Nullable String fromDateTime(@Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static <T> @NotNull List<T> fromList(@Nullable List<Document> documentList,
                                                @NotNull Function<Document, T> mapFunction) {
        if (documentList == null) return List.of();
        ArrayList<T> list = new ArrayList<>(documentList.size());
        for (Document document : documentList)
            list.add(mapFunction.apply(document));
        return list;
    }

    public static <T> @NotNull List<Document> toList(@Nullable Collection<T> entries,
                                                     @NotNull Function<T, Document> mapFunction) {
        if (entries == null) return List.of();
        ArrayList<Document> list = new ArrayList<>(entries.size());
        for (T entry : entries)
            list.add(mapFunction.apply(entry));
        return list;
    }

    public static @Nullable Location toLocation(@Nullable Document document) {
        if (document == null) return null;
        return new Location(document);
    }

    public static @Nullable Document fromLocation(@Nullable Location location) {
        if (location == null) return null;
        return location.toDocument();
    }
}
