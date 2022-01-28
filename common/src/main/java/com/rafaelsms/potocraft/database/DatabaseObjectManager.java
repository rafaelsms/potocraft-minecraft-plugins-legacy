package com.rafaelsms.potocraft.database;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseObjectManager {

    private static final Converter<?> DEFAULT_CONVERTER = new Converter<>() {
        @Override
        public Object parseToDocument(@Nullable Object object) {
            return object;
        }

        @Override
        public @Nullable Object fromDocument(@Nullable Object object) {
            return object;
        }
    };

    private final Map<Class<?>, Converter<?>> converterMap = new HashMap<>();

    public <T> void registerConverter(Class<T> tClass, Converter<T> tConverter) {
        this.converterMap.put(tClass, tConverter);
    }

    public <T> @Nullable T fromDocument(@NotNull Document document, Class<T> tClass) throws Database.DatabaseException {
        try {
            Constructor<T> constructor = tClass.getConstructor();
            T object = constructor.newInstance();
            setFields(object, document, tClass);
            return object;
        } catch (Exception exception) {
            throw new Database.DatabaseException(exception);
        }
    }

    public <T> Document toDocument(@NotNull T object) {
        Document document = new Document();
        extractFields(document, object, object.getClass());
        return document;
    }

    private <T> void setFields(T object, Document document, Class<?> superClass) {
        setFields(superClass.getFields(), object, document);
        setFields(superClass.getDeclaredFields(), object, document);
        if (superClass.getSuperclass() != null) {
            setFields(object, document, superClass.getSuperclass());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void setFields(@NotNull Field[] fields, @NotNull T object, @NotNull Document document) {
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DatabaseField.class)) {
                continue;
            }
            DatabaseField annotation = field.getAnnotation(DatabaseField.class);
            String name = annotation.name();
            if (annotation.name() == null || annotation.name().isBlank()) {
                name = field.getName();
            }

            try {
                Class<?> fieldType = field.getType();

                if (Map.class.isAssignableFrom(fieldType)) {
                    Map<Object, Object> hashMap = new HashMap<>();

                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    Class<?> valueType = (Class<?>) type.getActualTypeArguments()[1];
                    Converter<?> converter = converterMap.getOrDefault(valueType, DEFAULT_CONVERTER);

                    Map<?, ?> map = (Map<?, ?>) document.get(name);
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        hashMap.put(entry.getKey(), converter.fromDocument(entry.getValue()));
                    }

                    field.trySetAccessible();
                    field.set(object, hashMap);
                    continue;
                }
                if (Set.class.isAssignableFrom(fieldType)) {
                    HashSet<Object> hashSet = new HashSet<>();

                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    Class<?> valueType = (Class<?>) type.getActualTypeArguments()[0];
                    Converter<?> converter = converterMap.getOrDefault(valueType, DEFAULT_CONVERTER);

                    Set<?> list = (Set<?>) document.get(name);
                    for (Object entry : list) {
                        hashSet.add(converter.fromDocument(entry));
                    }

                    field.trySetAccessible();
                    field.set(object, hashSet);
                    continue;
                }
                if (List.class.isAssignableFrom(fieldType)) {
                    ArrayList<Object> arrayList = new ArrayList<>();

                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    Class<?> valueType = (Class<?>) type.getActualTypeArguments()[0];
                    Converter<?> converter = converterMap.getOrDefault(valueType, DEFAULT_CONVERTER);

                    Iterable<?> list = (Iterable<?>) document.get(name);
                    for (Object entry : list) {
                        arrayList.add(converter.fromDocument(entry));
                    }

                    field.set(object, arrayList);
                    continue;
                }

                field.trySetAccessible();
                Converter<Object> converter =
                        (Converter<Object>) converterMap.getOrDefault(fieldType, DEFAULT_CONVERTER);
                field.set(object, converter.fromDocument(document.get(name)));
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    private <T> void extractFields(Document document, T object, Class<?> superClass) {
        extractFields(document, object, superClass.getFields());
        extractFields(document, object, superClass.getDeclaredFields());
        if (superClass.getSuperclass() != null) {
            extractFields(document, object, superClass.getSuperclass());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void extractFields(@NotNull Document document, @NotNull T object, @NotNull Field[] fields) {
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DatabaseField.class)) {
                continue;
            }
            DatabaseField annotation = field.getAnnotation(DatabaseField.class);
            String name = annotation.name();
            if (annotation.name() == null || annotation.name().isBlank()) {
                name = field.getName();
            }

            try {
                Class<?> fieldType = field.getType();
                field.setAccessible(true);
                Converter<Object> converter =
                        (Converter<Object>) converterMap.getOrDefault(fieldType, DEFAULT_CONVERTER);
                Object value = converter.parseToDocument(field.get(object));
                document.putIfAbsent(name, value);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    public interface Converter<T> {

        Object parseToDocument(@Nullable T object);

        @Nullable T fromDocument(@Nullable Object object);

    }
}
