package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class Configuration extends YamlFile {

    public Configuration(@NotNull BlockProtectionPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public String getMongoURI() {
        return get("configuration.database.mongo_uri");
    }

    public String getMongoDatabaseName() {
        return get("configuration.database.database_name");
    }

    public String getMongoPlayerCollection() {
        return get("configuration.database.player_profile_collection");
    }

    public String getMongoRegionCollection() {
        return get("configuration.database.region_collection");
    }

    public long getMongoSavePlayersTaskTimer() {
        return Objects.requireNonNull(getLong("configuration.database.save_player_task_timer_ticks"));
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return get("configuration.database.is_exception_fatal");
    }

    public Integer getSelectionXZOffset() {
        return getInt("configuration.protection.selection.xz_offset");
    }

    public Integer getSelectionMinYOffset() {
        return getInt("configuration.protection.selection.min_y_offset");
    }

    public Integer getSelectionMaxYOffset() {
        return getInt("configuration.protection.selection.max_y_offset");
    }

    public int getDefaultBoxVolume() {
        return getSelectionXZOffset() * getSelectionXZOffset() * (getSelectionMinYOffset() + getSelectionMaxYOffset());
    }

    public int getSelectionTimeToLive() {
        return Objects.requireNonNull(getInt("configuration.protection.volume.selection_time_to_live_ticks"));
    }

    public Double getSelectionVolumeDefaultReward() {
        return getDouble("configuration.protection.volume.default_reward_per_block");
    }

    public Map<String, Double> getSelectionVolumeGroupReward() {
        return get("configuration.protection.volume.groups_reward_per_block");
    }

    public Integer getSelectionVolumeDefaultMaximum() {
        return getInt("configuration.protection.volume.default_maximum_volume");
    }

    public Map<String, Integer> getSelectionVolumeGroupMaximum() {
        return get("configuration.protection.volume.groups_maximum_volume");
    }

    public Component getFailedToFetchProfile() {
        return TextUtil.toComponent(get("language.errors.failed_to_fetch_profile"));
    }
}
