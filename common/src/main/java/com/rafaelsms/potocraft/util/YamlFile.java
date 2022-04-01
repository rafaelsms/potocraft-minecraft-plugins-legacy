package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class YamlFile {

    private final Map<String, Object> fileMap = Collections.synchronizedMap(new LinkedHashMap<>());

    private final File dataFolder;
    private final String fileName;

    public YamlFile(@NotNull File dataFolder, @NotNull String fileName) throws IOException {
        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            throw new IOException("Failed to create data folder");
        }
        this.dataFolder = dataFolder;
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
    protected <T> @Nullable T get(@NotNull String key, @Nullable T defaultValue) {
        return (T) fileMap.getOrDefault(key, defaultValue);
    }

    protected @Nullable Long getLong(@NotNull String key, @Nullable Long defaultValue) {
        return ((Number) fileMap.getOrDefault(key, defaultValue)).longValue();
    }

    protected @Nullable Integer getInt(@NotNull String key, @Nullable Integer defaultValue) {
        return ((Number) fileMap.getOrDefault(key, defaultValue)).intValue();
    }

    protected @Nullable Double getDouble(@NotNull String key, @Nullable Double defaultValue) {
        return ((Number) fileMap.getOrDefault(key, defaultValue)).doubleValue();
    }

    protected <T> @Nullable T get(@NotNull String key) {
        return get(key, null);
    }

    protected @Nullable Long getLong(@NotNull String key) {
        return getLong(key, null);
    }

    protected @Nullable Integer getInt(@NotNull String key) {
        return getInt(key, null);
    }

    protected @Nullable Double getDouble(@NotNull String key) {
        return getDouble(key, null);
    }

    /**
     * Load the file into a Map structure where the key-value pair describes the YAML file structure.
     *
     * @throws IOException if file reading fails
     */
    public void loadFile() throws IOException {
        File yamlFile = new File(dataFolder, fileName);
        if (!yamlFile.exists()) {
            // Copy from resources folder
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(fileName);
            Files.copy(Objects.requireNonNull(resource), yamlFile.toPath());
        }

        // Read file
        try (FileReader fileReader = new FileReader(yamlFile)) {
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
