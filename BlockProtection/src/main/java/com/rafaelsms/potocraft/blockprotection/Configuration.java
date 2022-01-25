package com.rafaelsms.potocraft.blockprotection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    private final @NotNull BlockProtectionPlugin plugin;

    public Configuration(@NotNull BlockProtectionPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        loadConfiguration();
        this.plugin = plugin;
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(Keys.MONGO_URI, "mongodb://localhost:27017");
        defaults.put(Keys.MONGO_DATABASE_NAME, "serverNameDb");
        defaults.put(Keys.MONGO_DATABASE_FAILURE_FATAL, false);

        defaults.put(Keys.PROTECTION_SELECTION_XZ_OFFSET, 6);
        defaults.put(Keys.PROTECTION_SELECTION_MIN_Y_OFFSET, 2);
        defaults.put(Keys.PROTECTION_SELECTION_MAX_Y_OFFSET, 4);

        return defaults;
    }

    public String getMongoURI() {
        return get(Keys.MONGO_URI);
    }

    public String getMongoDatabaseName() {
        return get(Keys.MONGO_DATABASE_NAME);
    }

    public boolean isMongoDatabaseExceptionFatal() {
        return get(Keys.MONGO_DATABASE_FAILURE_FATAL);
    }

    public int getSelectionXZOffset() {
        return get(Keys.PROTECTION_SELECTION_XZ_OFFSET);
    }

    public int getSelectionMinYOffset() {
        return get(Keys.PROTECTION_SELECTION_MIN_Y_OFFSET);
    }

    public int getSelectionMaxYOffset() {
        return get(Keys.PROTECTION_SELECTION_MAX_Y_OFFSET);
    }

    private static final class Keys {

        public static final String MONGO_URI = "configuration.database.mongo_uri";
        public static final String MONGO_DATABASE_NAME = "configuration.database.database_name";
        public static final String MONGO_DATABASE_FAILURE_FATAL = "configuration.database.exception_fatal";

        public static final String PROTECTION_SELECTION_XZ_OFFSET = "configuration.protection.selection.xz_offset";
        public static final String PROTECTION_SELECTION_MIN_Y_OFFSET =
                "configuration.protection.selection.min_y_offset";
        public static final String PROTECTION_SELECTION_MAX_Y_OFFSET =
                "configuration.protection.selection.max_y_offset";

    }
}
