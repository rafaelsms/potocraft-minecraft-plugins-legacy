package com.rafaelsms.potocraft.serverprofile;

import com.rafaelsms.potocraft.serverprofile.players.Home;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.players.TeleportRequest;
import com.rafaelsms.potocraft.serverprofile.util.CombatType;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import com.rafaelsms.potocraft.util.LuckPermsUtil;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Configuration extends YamlFile {

    private static final DecimalFormat floatFormatter = new DecimalFormat("0.000");

    public Configuration(@NotNull ServerProfilePlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public String getMongoURI() {
        return getOrThrow("configuration.database.mongo_uri");
    }

    public String getMongoDatabaseName() {
        return getOrThrow("configuration.database.database_name");
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return getOrThrow("configuration.database.is_exception_fatal");
    }

    public String getMongoPlayerProfileCollectionName() {
        return getOrThrow("configuration.database.player_profiles_collection");
    }

    public String getMongoWarpsCollectionName() {
        return getOrThrow("configuration.database.warps_collection");
    }

    public Integer getSavePlayersTaskTimerTicks() {
        return getIntOrNull("configuration.save_players_task_timer_in_ticks");
    }

    public Double getLocalChatRange() {
        return getDoubleOrNull("configuration.local_chat.range");
    }

    public Component getLocalChatFormat(@NotNull UUID senderId,
                                        @NotNull String senderName,
                                        @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.local_chat.local_format"), senderId, senderName, message);
    }

    public Component getLocalChatSpyFormat(@NotNull UUID senderId,
                                           @NotNull String senderName,
                                           @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.local_chat.spy_format"), senderId, senderName, message);
    }

    public Component getChatFormat(@NotNull UUID senderId, @NotNull String senderName, @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.local_chat.no_range_format"), senderId, senderName, message);
    }

    private Component getChatFormat(@Nullable String format,
                                    @NotNull UUID senderId,
                                    @NotNull String senderName,
                                    @NotNull Component message) {
        String prefix = LuckPermsUtil.getUncoloredPrefix(senderId).orElse("");
        String suffix = LuckPermsUtil.getUncoloredSuffix(senderId).orElse("");
        Component displayName = TextUtil.toLegacyComponent(prefix + senderName + suffix);
        return TextUtil.toComponent(format,
                                    Placeholder.parsed("prefix", prefix),
                                    Placeholder.parsed("username", senderName),
                                    Placeholder.parsed("suffix", suffix),
                                    Placeholder.component("displayname", displayName),
                                    Placeholder.component("message", message));
    }

    public Component getNobodyHeardYou() {
        return TextUtil.toComponent(getOrThrow("language.chat.nobody_heard_you"));
    }

    public Integer getTeleportDelayTicks() {
        return getIntOrNull("configuration.teleport.teleport_delay_in_ticks");
    }

    public Duration getTeleportCooldown() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.teleport.teleport_cooldown_in_seconds")));
    }

    public Duration getTeleportRequestDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.teleport.teleport_request_duration_in_seconds")));
    }

    public Duration getTotemCooldown() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull("configuration.totem_usage_cooldown_in_seconds")));
    }

    public Integer getDefaultHomeNumber() {
        return getIntOrNull("configuration.homes.default_number_of_homes");
    }

    public Long getHomeReplacementTimeout() {
        return getLongOrNull("configuration.homes.replace_home_timeout_ticks");
    }

    public Map<String, Integer> getHomePermissionGroups() {
        return getOrThrow("configuration.homes.home_number_permission_groups");
    }

    public Integer getMobCombatDurationTicks() {
        return getIntOrNull("configuration.combat.mob_combat_duration_ticks");
    }

    public Integer getPlayerCombatDurationTicks() {
        return getIntOrNull("configuration.combat.player_combat_duration_ticks");
    }

    public Duration getPlayerSafeRegionCombatTimeout() {
        Integer seconds = getIntOrNull("configuration.combat.player_combat_in_safe_region_timeout_seconds");
        return Duration.ofSeconds(Objects.requireNonNull(seconds));
    }

    public Boolean isOutOfCombatDeathDroppingItems() {
        return getOrThrow("configuration.combat.out_of_combat_death_should_drop_items");
    }

    public CombatType getDeathDroppingItemsCombatTypeRequired() {
        String name = getOrThrow("configuration.combat.minimum_combat_type_to_drop_items");
        return CombatType.valueOf(Objects.requireNonNull(name).toUpperCase());
    }

    public Boolean isOutOfCombatDeathDroppingExperience() {
        return getOrThrow("configuration.combat.out_of_combat_death_should_drop_experience");
    }

    public CombatType getDeathDroppingExperienceCombatTypeRequired() {
        String name = getOrThrow("configuration.combat.minimum_combat_type_to_drop_experience");
        return CombatType.valueOf(Objects.requireNonNull(name).toUpperCase());
    }

    public CombatType getPreventEnteringCombatTypeRequired() {
        String name = getOrThrow("configuration.combat.minimum_combat_type_to_prevent_entry");
        return CombatType.valueOf(Objects.requireNonNull(name).toUpperCase());
    }

    public Boolean isCombatLogOffDestroysTotemFirst() {
        return getOrThrow("configuration.combat.should_log_off_destroy_totem_first");
    }

    public Duration getPlayerKillerTimeout() {
        return Duration.ofSeconds(Objects.requireNonNull(getIntOrNull("configuration.combat.player_killer_time_seconds")));
    }

    public List<String> getCombatBlockedCommands() {
        return getOrThrow("configuration.combat.blocked_commands");
    }

    public Boolean isHardcoreModeEnabled() {
        return getOrThrow("configuration.combat.hardcore.enabled");
    }

    public Long getHardcoreDefaultBanTime() {
        return getLongOrNull("configuration.combat.hardcore.default_ban_time_in_seconds");
    }

    public Map<String, Integer> getHardcoreBanTimeGroups() {
        return getOrThrow("configuration.combat.hardcore.permission_ban_time_groups");
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(Objects.requireNonNull(getOrThrow("language.date_time_format")));
    }

    public Component getPlayersOnly() {
        return TextUtil.toComponent(getOrThrow("language.players_only"));
    }

    public Component getCouldNotLoadProfile() {
        return TextUtil.toComponent(getOrThrow("language.could_not_load_profile"));
    }

    public Component getCombatBarTitle() {
        return TextUtil.toComponent(getOrThrow("language.combat.bar_title"));
    }

    public Component getCombatBlockedCommand() {
        return TextUtil.toComponent(getOrThrow("language.combat.command_is_blocked_while_in_combat"));
    }

    public String getUnknownWorldName() {
        return getOrThrow("language.combat.unknown_world_name");
    }

    public Component getCombatDeathLocation(@NotNull Location location) {
        String worldName = getUnknownWorldName();
        World world = location.getWorld();
        if (world != null) {
            worldName = world.getName().replaceAll("_", " ");
        }
        return TextUtil.toComponent(getOrThrow("language.combat.your_death_location_is"),
                                    Placeholder.unparsed("world", worldName),
                                    Placeholder.unparsed("x", String.valueOf(location.getBlockX())),
                                    Placeholder.unparsed("y", String.valueOf(location.getBlockY())),
                                    Placeholder.unparsed("z", String.valueOf(location.getBlockZ())));
    }

    public Component getHardcoreBanMessage(@NotNull ZonedDateTime expirationDate) {
        return TextUtil.toComponent(getOrThrow("language.combat.hardcore.banned"),
                                    Placeholder.unparsed("expiration_date",
                                                         getDateTimeFormatter().format(expirationDate)));
    }

    public Component getTeleportBarTitle() {
        return TextUtil.toComponent(getOrThrow("language.teleport.bar_title"));
    }

    public Component getTeleportPlayerQuit() {
        return TextUtil.toComponent(getOrThrow("language.teleport.player_quit"));
    }

    public Component getTeleportPlayerInCombat() {
        return TextUtil.toComponent(getOrThrow("language.teleport.entered_combat"));
    }

    public Component getTeleportParticipantTeleporting() {
        return TextUtil.toComponent(getOrThrow("language.teleport.other_player_is_teleporting"));
    }

    public Component getTeleportParticipantInCombat() {
        return TextUtil.toComponent(getOrThrow("language.teleport.other_player_is_in_combat"));
    }

    public Component getTeleportAlreadyTeleporting() {
        return TextUtil.toComponent(getOrThrow("language.teleport.already_teleporting"));
    }

    public Component getTeleportDestinationUnavailable() {
        return TextUtil.toComponent(getOrThrow("language.teleport.destination_unavailable"));
    }

    public Component getTeleportInCooldown(long cooldownSeconds) {
        String messageFormat = getOrThrow("language.teleport.in_cooldown");
        return TextUtil.toComponent(messageFormat, Placeholder.unparsed("cooldown", String.valueOf(cooldownSeconds)));
    }

    public Component getTeleportFailed() {
        return TextUtil.toComponent(getOrThrow("language.teleport.failed"));
    }

    public Component getTeleportNoBackLocation() {
        return TextUtil.toComponent(getOrThrow("language.teleport.back.no_back_location"));
    }

    public Component getTeleportBackIsDeathLocation() {
        return TextUtil.toComponent(getOrThrow("language.teleport.back.back_is_death_location"));
    }

    public Component getTeleportPlayerNotFound() {
        return TextUtil.toComponent(getOrThrow("language.teleport.player_not_found"));
    }

    public Component getTeleportOfflineLocationNotFound() {
        return TextUtil.toComponent(getOrThrow("language.teleport.offline_player_location_not_found"));
    }

    public Component getTeleportHelp() {
        return TextUtil.toComponent(getOrThrow("language.teleport.help"));
    }

    public Component getTeleportRequestReceived(@NotNull String username) {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.teleport_received"),
                                    Placeholder.parsed("username", username));
    }

    public Component getTeleportHereRequestReceived(@NotNull String username) {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.teleport_here_received"),
                                    Placeholder.parsed("username", username));
    }

    public Component getTeleportRequestSent(@NotNull String username) {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.sent"),
                                    Placeholder.parsed("username", username));
    }

    public Component getTeleportRequestNotUpdated(@NotNull String username) {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.not_updated"),
                                    Placeholder.parsed("username", username));
    }

    public Component getTeleportRequestNotFound() {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.no_found"));
    }

    public Component getTeleportRequestCancelled() {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.request_cancelled"));
    }

    public Component getTeleportRequestManyFound(@NotNull Collection<TeleportRequest> requests) {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.many_found"),
                                    Placeholder.unparsed("list",
                                                         TextUtil.joinStrings(requests,
                                                                              ", ",
                                                                              request -> request.getRequester()
                                                                                                .getPlayer()
                                                                                                .getName())));
    }

    public Component getTeleportRequestsSentCancelled() {
        return TextUtil.toComponent(getOrThrow("language.teleport.requests.sent_requests_cancelled"));
    }

    public Component getTeleportHomeHelp() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.help"));

    }

    public Component getTeleportHomeList(@NotNull List<Home> homes) {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.list"),
                                    Placeholder.unparsed("list", TextUtil.joinStrings(homes, ", ", Home::getName)));
    }

    public Component getTeleportHomeMaxCapacity() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.at_max_capacity"));
    }

    public Component getTeleportHomeInvalidName() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.invalid_home_name"));
    }

    public Component getTeleportHomeCreateHelp() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.create_help"));
    }

    public Component getTeleportHomeCreated() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.created"));
    }

    public Component getTeleportHomeAlreadyExists() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.already_exists"));
    }

    public Component getTeleportHomeNotFound() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.not_found"));
    }

    public Component getTeleportHomeDeleted() {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.deleted"));
    }

    public Component getTeleportHomeDeleteHelp(@NotNull Collection<Home> homes) {
        return TextUtil.toComponent(getOrThrow("language.teleport.homes.delete_help"),
                                    Placeholder.unparsed("list", TextUtil.joinStrings(homes, ", ", Home::getName)));
    }

    public Component getTeleportWarpManageHelp() {
        return TextUtil.toComponent(getOrThrow("language.teleport.warps.manage.help"));
    }

    public Component getTeleportWarpManageSuccess() {
        return TextUtil.toComponent(getOrThrow("language.teleport.warps.manage.success"));
    }

    public Component getTeleportWarpManageDatabaseFailure() {
        return TextUtil.toComponent(getOrThrow("language.teleport.warps.manage.failure"));
    }

    public Component getTeleportWarpFailedToRetrieve() {
        return TextUtil.toComponent(getOrThrow("language.teleport.warps.failed_to_retrieve"));
    }

    public Component getTeleportWarpList(@NotNull Collection<Warp> warps) {
        return TextUtil.toComponent(getOrThrow("language.teleport.warps.list"),
                                    Placeholder.unparsed("list", TextUtil.joinStrings(warps, ", ", Warp::getName)));
    }

    public Component getTeleportWarpNotFound() {
        return TextUtil.toComponent(getOrThrow("language.teleport.warps.not_found"));
    }

    public Component getTeleportWorldList(@NotNull Collection<World> worlds) {
        return TextUtil.toComponent(getOrThrow("language.teleport.worlds.list"),
                                    Placeholder.unparsed("list", TextUtil.joinStrings(worlds, ", ", World::getName)));
    }

    public Component getTeleportWorldNotFound() {
        return TextUtil.toComponent(getOrThrow("language.teleport.worlds.not_found"));
    }

    public Component getTotemInCooldown() {
        return TextUtil.toComponent(getOrThrow("language.totem.totem_in_cooldown"));
    }

    public Component getTotemEnteredCooldown() {
        return TextUtil.toComponent(getOrThrow("language.totem.totem_entered_in_cooldown"));
    }

    public Component getKickMessageCouldNotLoadProfile() {
        return TextUtil.toComponent(getOrThrow("language.kick_messages.could_not_load_profile"));
    }

    public Component getTopNoPlayers() {
        return TextUtil.toComponent(getOrThrow("language.top.no_profiles_on_ranking"));
    }

    public Component getTopRanking(@NotNull List<Profile> rankedPlayers) {
        if (rankedPlayers.isEmpty()) {
            return getTopNoPlayers();
        }
        return TextUtil.toComponent(getOrThrow("language.top.ranking_list"),
                                    Placeholder.component("list",
                                                          Component.join(JoinConfiguration.builder()
                                                                                          .separator(Component.newline())
                                                                                          .build(),
                                                                         Util.convertList(rankedPlayers,
                                                                                          this::getRankingEntry))));
    }

    public Component getRankingEntry(@NotNull Profile profile) {
        int playerKills = profile.getPlayerKills();
        int playerDeaths = profile.getDeathCount();
        float killDeathRatio;
        if (playerDeaths == 0) {
            killDeathRatio = playerKills;
        } else {
            killDeathRatio = (float) playerKills / playerDeaths;
        }
        String killDeathRatioString = floatFormatter.format(killDeathRatio);
        return TextUtil.toComponent(getOrThrow("language.top.ranking_entry"),
                                    Placeholder.parsed("username", profile.getPlayerName()),
                                    Placeholder.unparsed("killdeathratio", killDeathRatioString),
                                    Placeholder.unparsed("killcount", String.valueOf(playerKills)),
                                    Placeholder.unparsed("deathcount", String.valueOf(playerDeaths)));
    }

    public @NotNull Component getEnteringRegionMessage(@NotNull Set<String> memberNames) {
        String memberList;
        if (memberNames.isEmpty()) {
            memberList = getRegionWithoutMembers();
        } else {
            memberList = TextUtil.joinStrings(memberNames, ", ", str -> str);
        }
        return TextUtil.toComponent(getOrThrow("language.regions.entering_region_members_names"),
                                    Placeholder.parsed("list", memberList));
    }

    public @NotNull Component getLeavingRegionMessage(@NotNull Set<String> memberNames) {
        String memberList;
        if (memberNames.isEmpty()) {
            memberList = getRegionWithoutMembers();
        } else {
            memberList = TextUtil.joinStrings(memberNames, ", ", str -> str);
        }
        return TextUtil.toComponent(getOrThrow("language.regions.leaving_region_members_names"),
                                    Placeholder.parsed("list", memberList));
    }

    public @NotNull String getUnknownPlayerName() {
        return Objects.requireNonNull(getOrThrow("language.regions.unknown_player"));
    }

    public @NotNull String getRegionWithoutMembers() {
        return Objects.requireNonNull(getOrThrow("language.regions.no_member"));
    }
}
