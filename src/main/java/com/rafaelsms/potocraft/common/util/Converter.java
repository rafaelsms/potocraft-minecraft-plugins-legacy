package com.rafaelsms.potocraft.common.util;

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
        for (Document document : documentList) {
            list.add(mapFunction.apply(document));
        }
        return list;
    }

    public static <T> @NotNull List<Document> toList(@Nullable Collection<T> entries,
                                                     @NotNull Function<T, Document> mapFunction) {
        if (entries == null) return List.of();
        ArrayList<Document> list = new ArrayList<>(entries.size());
        for (T entry : entries) {
            list.add(mapFunction.apply(entry));
        }
        return list;
    }

    public static <T, U> void toDocumentList(@NotNull Document document, @NotNull String key,
                                             @Nullable Collection<T> tList, @NotNull Function<T, U> objectToListMapper) {
        if (tList == null) {
            document.put(key, null);
            return;
        }
        ArrayList<U> list = new ArrayList<>(tList.size());
        for (T entry : tList) {
            list.add(objectToListMapper.apply(entry));
        }
        document.put(key, list);
    }

    public static <T, U> @NotNull List<T> fromDocumentList(@NotNull Document document, @NotNull String key,
                                                           @NotNull Function<U, T> listToObjectMapper,
                                                           @NotNull Class<U> uClass) {
        List<U> uList = document.getList(key, uClass);
        if (uList == null || uList.isEmpty()) return List.of();
        ArrayList<T> list = new ArrayList<>(uList.size());
        for (U entry : uList) {
            list.add(listToObjectMapper.apply(entry));
        }
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
