package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class YamlFile {

    private final Map<String, Object> fileMap = Collections.synchronizedMap(new LinkedHashMap<>());

    private final Path filePath;
    private final String fileName;

    public YamlFile(@NotNull File dataFolder, @NotNull String fileName) throws IOException {
        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            throw new IOException("Failed to create data folder");
        }
        this.filePath = dataFolder.toPath().resolve(fileName);
        this.fileName = fileName;
        loadFile();
    }

    /**
     * Get the value for specified key.
     *
     * @param key key string
     * @param <T> expected type of the value
     * @return the value loaded from the YAML file or the given default value, which can be null
     */
    @SuppressWarnings("unchecked")
    protected <T> @NotNull T getOrFallback(@NotNull String key, @NotNull T defaultValue) {
        return (T) fileMap.getOrDefault(key, defaultValue);
    }

    protected long getLongOrFallback(@NotNull String key, long defaultValue) {
        return ((Number) getOrFallback(key, defaultValue)).longValue();
    }

    protected int getIntOrFallback(@NotNull String key, int defaultValue) {
        return ((Number) getOrFallback(key, defaultValue)).intValue();
    }

    protected float getFloatOrFallback(@NotNull String key, float defaultValue) {
        return ((Number) getOrFallback(key, defaultValue)).floatValue();
    }

    protected double getDoubleOrFallback(@NotNull String key, double defaultValue) {
        return ((Number) getOrFallback(key, defaultValue)).doubleValue();
    }

    @SuppressWarnings("unchecked")
    protected <T> @NotNull T getOrThrow(@NotNull String key) {
        return Optional.ofNullable((T) getOrNull(key)).orElseThrow();
    }

    protected int getIntOrThrow(@NotNull String key) {
        return Optional.ofNullable((Number) getOrNull(key)).orElseThrow().intValue();
    }

    protected long getLongOrThrow(@NotNull String key) {
        return Optional.ofNullable((Number) getOrNull(key)).orElseThrow().longValue();
    }

    protected float getFloatOrThrow(@NotNull String key) {
        return Optional.ofNullable((Number) getOrNull(key)).orElseThrow().floatValue();
    }

    protected double getDoubleOrThrow(@NotNull String key) {
        return Optional.ofNullable((Number) getOrNull(key)).orElseThrow().doubleValue();
    }

    @SuppressWarnings("unchecked")
    protected <T> @Nullable T getOrNull(@NotNull String key) {
        return (T) fileMap.getOrDefault(key, null);
    }

    protected @Nullable Long getLongOrNull(@NotNull String key) {
        return Util.convert(getOrNull(key), o -> ((Number) o).longValue());
    }

    protected @Nullable Integer getIntOrNull(@NotNull String key) {
        return Util.convert(getOrNull(key), o -> ((Number) o).intValue());
    }

    protected @Nullable Float getFloatOrNull(@NotNull String key) {
        return Util.convert(getOrNull(key), o -> ((Number) o).floatValue());
    }

    protected @Nullable Double getDoubleOrNull(@NotNull String key) {
        return Util.convert(getOrNull(key), o -> ((Number) o).doubleValue());
    }

    /**
     * Load the file into a Map structure where the key-value pair describes the YAML file structure.
     *
     * @throws IOException if file reading fails
     */
    public void loadFile() throws IOException {
        if (!Files.exists(filePath)) {
            // Copy from resources folder
            try (InputStream resource = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
                Files.copy(Objects.requireNonNull(resource), filePath);
            }
        }

        // Read file
        try (BufferedReader fileReader = Files.newBufferedReader(filePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> flatMap = new HashMap<>();
            Map<String, Object> treeMap = yaml.load(fileReader);
            flattenMap(flatMap, treeMap, "");
            fileMap.putAll(flatMap);
        }
    }

    /**
     * Flattens the tree-like map into a key that describes the tree structure.
     *
     * @param sinkMap   the output flat map
     * @param sourceMap the tree-like input map
     * @param path      used in recursion, initialize with an empty string
     */
    @SuppressWarnings("unchecked")
    private void flattenMap(Map<String, Object> sinkMap, Map<String, Object> sourceMap, String path) {
        String recursionPath;
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            if (path.isEmpty()) {
                recursionPath = entry.getKey();
            } else {
                recursionPath = "%s.%s".formatted(path, entry.getKey());
            }
            if (entry.getValue() instanceof Map<?, ?> map) {
                Map<String, Object> stringObjectMap = (Map<String, Object>) map;
                flattenMap(sinkMap, stringObjectMap, recursionPath);
                sinkMap.put(recursionPath, stringObjectMap);
            } else {
                sinkMap.put(recursionPath, entry.getValue());
            }
        }
    }
}
