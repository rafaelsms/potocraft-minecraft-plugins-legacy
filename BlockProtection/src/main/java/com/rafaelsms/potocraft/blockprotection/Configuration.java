package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        return getOrThrow("configuration.database.mongo_uri");
    }

    public String getMongoDatabaseName() {
        return getOrThrow("configuration.database.database_name");
    }

    public String getMongoPlayerCollection() {
        return getOrThrow("configuration.database.player_profile_collection");
    }

    public long getMongoSavePlayersTaskTimer() {
        return Objects.requireNonNull(getLongOrNull("configuration.database.save_player_task_timer_ticks"));
    }

    public String getBaseGlobalRegionId() {
        return getOrThrow("configuration.protection.base_global_region");
    }

    public List<String> getProtectedWorlds() {
        List<String> worldNames = getOrThrow("configuration.protection.protected_worlds");
        return Util.convertList(worldNames, String::toLowerCase);
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return getOrThrow("configuration.database.is_exception_fatal");
    }

    public int getSelectionXZOffset() {
        return Objects.requireNonNull(getIntOrNull("configuration.protection.selection.xz_offset"));
    }

    public int getSelectionMinYProtection() {
        return Objects.requireNonNull(getIntOrNull("configuration.protection.selection.min_y_protected"));
    }

    public int getParticlePeriodTicks() {
        return Objects.requireNonNull(getIntOrNull("configuration.protection.selection.particles_period_ticks"));
    }

    public Material getSelectionWandMaterial() {
        String materialName =
                Objects.requireNonNull(getOrThrow("configuration.protection.selection.selection_wand_material"));
        return Objects.requireNonNull(Material.matchMaterial(materialName));
    }

    public int getDefaultBoxArea() {
        return (2 * (getSelectionXZOffset() + 1)) * (2 * (getSelectionXZOffset() + 1));
    }

    public int getSelectionTimeToLive() {
        return Objects.requireNonNull(getIntOrNull("configuration.protection.area.selection_time_to_live_ticks"));
    }

    public double getSelectionMaxXZRatio() {
        return Objects.requireNonNull(getDoubleOrNull("configuration.protection.area.maximum_xz_selection_ratio"));
    }

    public Double getSelectionAreaDefaultReward() {
        return getDoubleOrNull("configuration.protection.area.default_reward_per_block");
    }

    public Map<String, Double> getSelectionAreaGroupReward() {
        return getOrThrow("configuration.protection.area.groups_reward_per_block");
    }

    public int getOverallMaximumArea() {
        return Objects.requireNonNull(getIntOrNull("configuration.protection.area.overall_maximum_area"));
    }

    public int getSelectionAreaDefaultMaximum() {
        return Objects.requireNonNull(getIntOrNull("configuration.protection.area.default_maximum_area"));
    }

    public Map<String, Integer> getSelectionAreaGroupMaximum() {
        return getOrThrow("configuration.protection.area.groups_maximum_area");
    }

    public Double getDeletionDefaultPayback() {
        return getDoubleOrNull("configuration.protection.area.default_deletion_payback");
    }

    public Map<String, Double> getGroupDeletionPayback() {
        return getOrThrow("configuration.protection.area.groups_deletion_payback");
    }

    public Component getPlayerOnlyCommand() {
        return TextUtil.toComponent(getOrThrow("language.errors.player_only_command"));
    }

    public Component getPlayerNotFound() {
        return TextUtil.toComponent(getOrThrow("language.errors.player_not_found"));
    }

    public Component getRegionNotFound() {
        return TextUtil.toComponent(getOrThrow("language.errors.region_not_found"));
    }

    public Component getFailedToFetchProfile() {
        return TextUtil.toComponent(getOrThrow("language.errors.failed_to_fetch_profile"));
    }

    public Component getFailedToFetchRegions() {
        return TextUtil.toComponent(getOrThrow("language.errors.failed_to_get_region_manager"));
    }

    public Component getNoRegionPermission() {
        return TextUtil.toComponent(getOrThrow("language.errors.no_region_permission"));
    }

    public Component getProtectHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.help"));
    }

    public Component getProtectClearedSelection() {
        return TextUtil.toComponent(getOrThrow("language.command.cleared_selection"));
    }

    public Component getProtectDeleteHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.delete_help"));
    }

    public Component getProtectExpandHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.expand_help"));
    }

    public Component getProtectRegionExpanded() {
        return TextUtil.toComponent(getOrThrow("language.command.region_expanded"));
    }

    public Component getProtectCreateHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.create_help"));
    }

    public Component getProtectRegionAlreadyExists() {
        return TextUtil.toComponent(getOrThrow("language.command.create_already_exists"));
    }

    public Component getProtectInvalidName() {
        return TextUtil.toComponent(getOrThrow("language.command.create_invalid_name"));
    }

    public Component getProtectRegionDeleted() {
        return TextUtil.toComponent(getOrThrow("language.command.region_deleted"));
    }

    public Component getProtectToggleMemberHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.toggle_member_help"));
    }

    public Component getProtectToggleOwnerHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.toggle_owner_help"));
    }

    public Component getProtectCantToggleCreator() {
        return TextUtil.toComponent(getOrThrow("language.command.cant_toggle_creator"));
    }

    public Component getProtectPlayerIsOwner() {
        return TextUtil.toComponent(getOrThrow("language.command.player_is_owner"));
    }

    public Component getProtectOnlyCreatorCanOperate() {
        return TextUtil.toComponent(getOrThrow("language.command.creator_only"));
    }

    public Component getProtectAreaAvailable(int minimumArea, int areaAvailable, int areaLimit, double reward) {
        return TextUtil.toComponent(getOrThrow("language.command.area_available"),
                                    Placeholder.unparsed("minimum_area", String.valueOf(minimumArea)),
                                    Placeholder.unparsed("area_available", String.valueOf(areaAvailable)),
                                    Placeholder.unparsed("area_limit", String.valueOf(areaLimit)),
                                    Placeholder.unparsed("reward", new DecimalFormat("0.00").format(reward)));
    }

    public Component getProtectPlayerAddedToRegion(@NotNull String playerName) {
        return TextUtil.toComponent(getOrThrow("language.command.player_added_to_region"),
                                    Placeholder.unparsed("username", playerName));
    }

    public Component getProtectPlayerRemovedFromRegion(@NotNull String playerName) {
        return TextUtil.toComponent(getOrThrow("language.command.player_removed_from_region"),
                                    Placeholder.unparsed("username", playerName));
    }

    public Component getSelectionWorldNotProtected() {
        return TextUtil.toComponent(getOrThrow("language.selection.world_not_protected"));
    }

    public Component getSelectionOnDifferentWorld() {
        return TextUtil.toComponent(getOrThrow("language.selection.selection_is_on_different_world"));
    }

    public Component getSelectionMaximumAreaExceeded() {
        return TextUtil.toComponent(getOrThrow("language.selection.maximum_area_exceeded"));
    }

    public Component getSelectionNotEnoughArea() {
        return TextUtil.toComponent(getOrThrow("language.selection.not_enough_area"));
    }

    public Component getSelectionTooNarrow() {
        return TextUtil.toComponent(getOrThrow("language.selection.too_narrow"));
    }

    public Component getSelectionMinimumAreaRequired() {
        return TextUtil.toComponent(getOrThrow("language.selection.minimum_area_required"));
    }

    public Component getSelectionInvalidLocation() {
        return TextUtil.toComponent(getOrThrow("language.selection.invalid_location"));
    }

    public Component getSelectionInsideOtherRegion() {
        return TextUtil.toComponent(getOrThrow("language.selection.selection_inside_other_region"));
    }

    public Component getSelectionStarted() {
        return TextUtil.toComponent(getOrThrow("language.selection.selection_started"));
    }

    public Component getSelectionRequired() {
        return TextUtil.toComponent(getOrThrow("language.selection.selection_required"));
    }

    public Component getSelectionForExpandingOnly() {
        return TextUtil.toComponent(getOrThrow("language.selection.selection_for_expanding_only"));
    }

    public Component getRegionRequired() {
        return TextUtil.toComponent(getOrThrow("language.selection.region_required"));
    }

    public String getGreetingTitle() {
        return TextUtil.toColorizedString(TextUtil.toComponent(getOrThrow("language.regions.greeting_title")));
    }

    public String getLeavingTitle() {
        return TextUtil.toColorizedString(TextUtil.toComponent(getOrThrow("language.regions.leaving_title")));
    }
}
