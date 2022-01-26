package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
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
        defaults.put(Keys.MONGO_PLAYER_COLLECTION_NAME, "playerProtectionProfile");
        defaults.put(Keys.MONGO_DATABASE_FAILURE_FATAL, false);

        defaults.put(Keys.PROTECTION_SELECTION_XZ_OFFSET, 6);
        defaults.put(Keys.PROTECTION_SELECTION_MIN_Y_OFFSET, 2);
        defaults.put(Keys.PROTECTION_SELECTION_MAX_Y_OFFSET, 4);

        defaults.put(Keys.PROTECTION_VOLUME_DEFAULT_REWARD_PER_BLOCK, 0.20);
        defaults.put(Keys.PROTECTION_VOLUME_GROUPS_REWARD_PER_BLOCK, Map.of("potocraft.protection.reward.vip", 0.75));
        defaults.put(Keys.PROTECTION_VOLUME_DEFAULT_MAXIMUM, 15_000);
        defaults.put(Keys.PROTECTION_VOLUME_GROUPS_MAXIMUM, Map.of("potocraft.protection.volume.vip", 50_000));

        return defaults;
    }

    public String getMongoURI() {
        return get(Keys.MONGO_URI);
    }

    public String getMongoDatabaseName() {
        return get(Keys.MONGO_DATABASE_NAME);
    }

    public String getMongoPlayerCollection() {
        return get(Keys.MONGO_PLAYER_COLLECTION_NAME);
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

    public int getDefaultVolume() {
        return getSelectionXZOffset() * getSelectionXZOffset() * (getSelectionMinYOffset() + getSelectionMaxYOffset());
    }

    public double getSelectionVolumeDefaultReward() {
        return get(Keys.PROTECTION_VOLUME_DEFAULT_REWARD_PER_BLOCK);
    }

    public Map<String, Double> getSelectionVolumeGroupReward() {
        return get(Keys.PROTECTION_VOLUME_GROUPS_REWARD_PER_BLOCK);
    }

    public int getSelectionVolumeDefaultMaximum() {
        return get(Keys.PROTECTION_VOLUME_DEFAULT_MAXIMUM);
    }

    public Map<String, Integer> getSelectionVolumeGroupMaximum() {
        return get(Keys.PROTECTION_VOLUME_GROUPS_MAXIMUM);
    }

    public Component getFailedToFetchProfile() {
        return TextUtil.toComponent(get(Keys.LANGUAGE_FAILED_TO_FETCH_PROFILE)).build();
    }

    private static final class Keys {

        public static final String MONGO_URI = "configuration.database.mongo_uri";
        public static final String MONGO_DATABASE_NAME = "configuration.database.database_name";
        public static final String MONGO_PLAYER_COLLECTION_NAME = "configuration.database.player_profile_collection";
        public static final String MONGO_DATABASE_FAILURE_FATAL = "configuration.database.exception_fatal";

        public static final String PROTECTION_SELECTION_XZ_OFFSET = "configuration.protection.selection.xz_offset";
        public static final String PROTECTION_SELECTION_MIN_Y_OFFSET =
                "configuration.protection.selection.min_y_offset";
        public static final String PROTECTION_SELECTION_MAX_Y_OFFSET =
                "configuration.protection.selection.max_y_offset";

        public static final String PROTECTION_VOLUME_DEFAULT_REWARD_PER_BLOCK =
                "configuration.protection.volume.default_reward_per_block";
        public static final String PROTECTION_VOLUME_GROUPS_REWARD_PER_BLOCK =
                "configuration.protection.volume.groups_reward_per_block";
        public static final String PROTECTION_VOLUME_DEFAULT_MAXIMUM =
                "configuration.protection.volume.default_maximum_volume";
        public static final String PROTECTION_VOLUME_GROUPS_MAXIMUM =
                "configuration.protection.volume.groups_maximum_volume";


        public static final String LANGUAGE_FAILED_TO_FETCH_PROFILE = "language.errors.failed_to_fetch_profile";

    }
}
