package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
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

    public String getBaseGlobalRegionId() {
        return get("configuration.protection.base_global_region");
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
        return (2 * (getSelectionXZOffset() + 1)) *
               (2 * (getSelectionXZOffset() + 1)) *
               (getSelectionMinYOffset() + getSelectionMaxYOffset() + 1);
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

    public Component getPlayerNotFound() {
        return TextUtil.toComponent(get("language.errors.player_not_found"));
    }

    public Component getRegionNotFound() {
        return TextUtil.toComponent(get("language.errors.region_not_found"));
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

    public Component getProtectHelp() {
        return TextUtil.toComponent(get("language.command.help"));
    }

    public Component getProtectClearedSelection() {
        return TextUtil.toComponent(get("language.command.cleared_selection"));
    }

    public Component getProtectDeleteHelp() {
        return TextUtil.toComponent(get("language.command.delete_help"));
    }

    public Component getProtectExpandHelp() {
        return TextUtil.toComponent(get("language.command.expand_help"));
    }

    public Component getProtectRegionExpanded() {
        return TextUtil.toComponent(get("language.command.region_expanded"));
    }

    public Component getProtectCreateHelp() {
        return TextUtil.toComponent(get("language.command.create_help"));
    }

    public Component getProtectRegionAlreadyExists() {
        return TextUtil.toComponent(get("language.command.create_already_exists"));
    }

    public Component getProtectInvalidName() {
        return TextUtil.toComponent(get("language.command.create_invalid_name"));
    }

    public Component getProtectRegionDeleted() {
        return TextUtil.toComponent(get("language.command.region_deleted"));
    }

    public Component getProtectToggleMemberHelp() {
        return TextUtil.toComponent(get("language.command.toggle_member_help"));
    }

    public Component getProtectToggleOwnerHelp() {
        return TextUtil.toComponent(get("language.command.toggle_owner_help"));
    }

    public Component getProtectCantToggleCreator() {
        return TextUtil.toComponent(get("language.command.cant_toggle_creator"));
    }

    public Component getProtectPlayerIsOwner() {
        return TextUtil.toComponent(get("language.command.player_is_owner"));
    }

    public Component getProtectOnlyCreatorCanOperate() {
        return TextUtil.toComponent(get("language.command.creator_only"));
    }

    public Component getProtectVolumeAvailable(int minimumVolume,
                                               int volumeAvailable,
                                               int maximumVolume,
                                               double reward) {
        return TextUtil.toComponent(get("language.command.volume_available"),
                                    Template.of("minimum_volume", String.valueOf(minimumVolume)),
                                    Template.of("volume_available", String.valueOf(volumeAvailable)),
                                    Template.of("volume_limit", String.valueOf(maximumVolume)),
                                    Template.of("reward", new DecimalFormat("0.00").format(reward)));
    }

    public Component getProtectPlayerAddedToRegion(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.command.player_added_to_region"),
                                    Template.of("username", playerName));
    }

    public Component getProtectPlayerRemovedFromRegion(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.command.player_removed_from_region"),
                                    Template.of("username", playerName));
    }

    public Component getSelectionWorldNotProtected() {
        return TextUtil.toComponent(get("language.selection.world_not_protected"));
    }

    public Component getSelectionOnDifferentWorld() {
        return TextUtil.toComponent(get("language.selection.selection_is_on_different_world"));
    }

    public Component getSelectionMaximumVolumeExceeded() {
        return TextUtil.toComponent(get("language.selection.maximum_volume_exceeded"));
    }

    public Component getSelectionNotEnoughVolume() {
        return TextUtil.toComponent(get("language.selection.not_enough_volume"));
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

    public Component getSelectionRequired() {
        return TextUtil.toComponent(get("language.selection.selection_required"));
    }

    public Component getRegionRequired() {
        return TextUtil.toComponent(get("language.selection.region_required"));
    }

    public String getGreetingTitle() {
        return TextUtil.toColorizedString(TextUtil.toComponent(get("language.regions.greeting_title")));
    }

    public String getLeavingTitle() {
        return TextUtil.toColorizedString(TextUtil.toComponent(get("language.regions.leaving_title")));
    }
}
