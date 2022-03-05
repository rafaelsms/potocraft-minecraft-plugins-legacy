package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
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

    public long getMongoSavePlayersTaskTimer() {
        return Objects.requireNonNull(getLong("configuration.database.save_player_task_timer_ticks"));
    }

    public List<String> getProtectedWorlds() {
        return get("configuration.protection.protected_worlds");
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return get("configuration.database.is_exception_fatal");
    }

    public int getSelectionXZOffset() {
        return Objects.requireNonNull(getInt("configuration.protection.selection.xz_offset"));
    }

    public int getSelectionMinYOffset() {
        return Objects.requireNonNull(getInt("configuration.protection.selection.min_y_offset"));
    }

    public int getSelectionMaxYOffset() {
        return Objects.requireNonNull(getInt("configuration.protection.selection.max_y_offset"));
    }

    public int getParticlePeriodTicks() {
        return Objects.requireNonNull(getInt("configuration.protection.selection.particles_period_ticks"));
    }

    public Material getSelectionWandMaterial() {
        String materialName = Objects.requireNonNull(get("configuration.protection.selection.selection_wand_material"));
        return Objects.requireNonNull(Material.matchMaterial(materialName));
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

    public Double getDeletionDefaultPayback() {
        return getDouble("configuration.protection.volume.default_deletion_payback");
    }

    public Map<String, Double> getGroupDeletionPayback() {
        return get("configuration.protection.volume.groups_deletion_payback");
    }

    public int getOverallMaximumVolume() {
        return Objects.requireNonNull(getInt("configuration.protection.volume.overall_maximum_volume"));
    }

    public int getSelectionVolumeDefaultMaximum() {
        return Objects.requireNonNull(getInt("configuration.protection.volume.default_maximum_volume"));
    }

    public Map<String, Integer> getSelectionVolumeGroupMaximum() {
        return get("configuration.protection.volume.groups_maximum_volume");
    }

    public Component getPlayerOnlyCommand() {
        return TextUtil.toComponent(get("language.errors.player_only_command"));
    }

    public Component getFailedToFetchProfile() {
        return TextUtil.toComponent(get("language.errors.failed_to_fetch_profile"));
    }

    public Component getFailedToFetchRegions() {
        return TextUtil.toComponent(get("language.errors.failed_to_get_region_manager"));
    }

    public Component getNoRegionPermission() {
        return TextUtil.toComponent(get("language.errors.no_region_permission"));
    }

    public Component getSelectionWorldNotProtected() {
        return TextUtil.toComponent(get("language.selection.world_not_protected"));
    }

    public Component getSelectionMaximumVolumeExceeded() {
        return TextUtil.toComponent(get("language.selection.maximum_volume_exceeded"));
    }

    public Component getSelectionVolumeExceeded() {
        return TextUtil.toComponent(get("language.selection.volume_exceeded"));
    }

    public Component getSelectionMinimumVolumeRequired() {
        return TextUtil.toComponent(get("language.selection.minimum_volume_required"));
    }

    public Component getSelectionVolumeExceedPermission() {
        return TextUtil.toComponent(get("language.selection.volume_exceed_permission"));
    }

    public Component getSelectionInvalidLocation() {
        return TextUtil.toComponent(get("language.selection.invalid_location"));
    }

    public Component getSelectionInsideOtherRegion() {
        return TextUtil.toComponent(get("language.selection.selection_inside_other_region"));
    }

    public Component getSelectionStarted() {
        return TextUtil.toComponent(get("language.selection.selection_started"));
    }
}
