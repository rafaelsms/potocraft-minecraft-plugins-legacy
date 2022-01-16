package com.rafaelsms.potocraft;

import com.rafaelsms.potocraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Configuration {

    private final Map<String, Object> configuration = Collections.synchronizedMap(new LinkedHashMap<>());

    private final File configurationFile;

    public Configuration(@NotNull File configurationFile) {
        this.configurationFile = configurationFile;
    }

    /**
     * Writes the default configuration to be written on file if file does not exists.
     */
    protected abstract @Nullable Map<String,Object> getDefaults();

    /**
     * Get the configuration value for specified key. It may return null if the key is not initialized inside the
     * {@link #getDefaults()} method or if the configuration file was generated on a older file version.
     *
     * @param key configuration key
     * @param <T> expected type of the configuration
     * @return null or the configuration value
     */
    @SuppressWarnings("unchecked")
    protected <T> T get(@NotNull String key) {
        return (T) configuration.get(key);
    }

    /**
     * Load configuration from file into the configuration map.
     *
     * @throws IOException if file reading fails
     */
    public void loadConfiguration() throws IOException {
        if (!configurationFile.exists()) {
            configuration.putAll(Util.getOrElse(getDefaults(), Map.of()));
            writeConfiguration();
        }

        try (FileReader fileReader = new FileReader(configurationFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> flatMap = new HashMap<>();
            Map<String, Object> treeMap = yaml.load(fileReader);
            flattenMap(flatMap, treeMap, "");
            configuration.putAll(flatMap);
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
                flattenMap(sinkMap, (Map<String, Object>) map, recursionPath);
            } else {
                sinkMap.put(recursionPath, entry.getValue());
            }
        }
    }

    /**
     * Writes the current configuration map into the file.
     *
     * @throws IOException if file writing fails
     */
    public void writeConfiguration() throws IOException {
        Map<String, Object> serializingMap = new HashMap<>();
        treelifyMap(serializingMap, configuration);

        try (FileWriter fileWriter = new FileWriter(configurationFile)) {
            DumperOptions options = new DumperOptions();
            options.setIndent(4);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            yaml.dump(serializingMap, fileWriter);
        }
    }

    /**
     * Make a tree-like map from a flat map where the keys describe the tree structure using dots.
     *
     * @param sinkMap   the output tree-like map
     * @param sourceMap the flat input map
     */
    @SuppressWarnings("unchecked")
    private void treelifyMap(Map<String, Object> sinkMap, Map<String, Object> sourceMap) {
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            Map<String, Object> root = sinkMap;
            String[] path = entry.getKey().split("\\.");
            for (int i = 0; i < path.length; i++) {
                String div = path[i];
                if (i + 1 == path.length) {
                    root.put(div, entry.getValue());
                } else {
                    HashMap<String, Object> map = (HashMap<String, Object>) root.getOrDefault(div, new HashMap<>());
                    root.put(div, map);
                    root = map;
                }
            }
        }
    }


}
