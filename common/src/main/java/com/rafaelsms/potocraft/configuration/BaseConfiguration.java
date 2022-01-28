package com.rafaelsms.potocraft.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseConfiguration extends Configuration {

    private final HashMap<String, Object> defaultConfiguration = new HashMap<>();

    protected BaseConfiguration(@NotNull File dataFolder, @NotNull String fileName) throws IOException {
        super(dataFolder, fileName);
        loadConfiguration();
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        setDefaults();
        return defaultConfiguration;
    }

    /**
     * Set default key-value to the configuration file. This method should be called in {@link this#setDefaults()} to be
     * written on disk when first initializing the plugin or to append to the default configuration.
     *
     * @param key   key
     * @param value default value
     * @param <T>   type of value
     */
    protected <T> void setDefault(@NotNull String key, @Nullable T value) {
        this.defaultConfiguration.putIfAbsent(key, value);
    }

    /**
     * Sets the default configuration.
     *
     * @see #setDefault(String, Object) to set each key-value pair
     */
    protected void setDefaults() {
        for (Constants constants : Constants.values()) {
            setDefault(constants.getKey(), constants.getDefaultValue());
        }
    }

    protected Long getLong(Constants key) {
        Number number = get(key.getKey());
        return number.longValue();
    }

    protected Integer getInt(Constants key) {
        Number number = get(key.getKey());
        return number.intValue();
    }

    protected String getString(Constants key) {
        return get(key.getKey());
    }

    protected Boolean getBoolean(Constants key) {
        return get(key.getKey());
    }

    public enum Constants {

        MONGO_URI("configuration.database.uri", ""),
        MONGO_DATABASE_NAME("configuration.database.database_name", "serverNameDb"),
        MONGO_IS_EXCEPTION_FATAL("configuration.database.is_database_errors_fatal", true),
        ;

        private final String key;
        private final Object defaultValue;

        Constants(@NotNull String key, @Nullable Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
