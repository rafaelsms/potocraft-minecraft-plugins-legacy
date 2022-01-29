package com.rafaelsms.potocraft.serverprofile;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.serverprofile.players.Home;
import com.rafaelsms.potocraft.serverprofile.players.TeleportRequest;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Configuration extends YamlFile {

    public Configuration(@NotNull ServerProfilePlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public String getMongoURI() {
        return get("configuration.database.mongo_uri");
    }

    public String getMongoDatabaseName() {
        return get("configuration.database.database_name");
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return get("configuration.database.is_exception_fatal");
    }

    public String getMongoPlayerProfileCollectionName() {
        return get("configuration.database.player_profiles_collection");
    }

    public String getMongoWarpsCollectionName() {
        return get("configuration.database.warps_collection");
    }

    public Integer getSavePlayersTaskTimerTicks() {
        return getInt("configuration.save_players_task_timer_in_ticks");
    }

    public Double getLocalChatRange() {
        return getDouble("configuration.local_chat.range");
    }

    public Component getLocalChatFormat(@NotNull UUID senderId,
                                        @NotNull String senderName,
                                        @NotNull Component message) {
        return getChatFormat(get("configuration.local_chat.local_format"), senderId, senderName, message);
    }

    public Component getLocalChatSpyFormat(@NotNull UUID senderId,
                                           @NotNull String senderName,
                                           @NotNull Component message) {
        return getChatFormat(get("configuration.local_chat.spy_format"), senderId, senderName, message);
    }

    public Component getChatFormat(@NotNull UUID senderId, @NotNull String senderName, @NotNull Component message) {
        return getChatFormat(get("configuration.local_chat.no_range_format"), senderId, senderName, message);
    }

    private Component getChatFormat(@Nullable String format, @NotNull UUID senderId,
                                    @NotNull String senderName,
                                    @NotNull Component message) {
        return TextUtil.toComponent(format)
                       .replace("%username%", senderName)
                       .replace("%prefix%", TextUtil.getPrefix(senderId))
                       .replace("%suffix%", TextUtil.getSuffix(senderId))
                       .replace("%message%", message)
                       .build();
    }

    public Integer getTeleportDelayTicks() {
        return getInt("configuration.teleport.teleport_delay_in_ticks");
    }

    public Duration getTeleportCooldown() {
        return Duration.ofSeconds(Objects.requireNonNull(getLong("configuration.teleport.teleport_cooldown_in_seconds")));
    }

    public Duration getTeleportRequestDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getLong(
                "configuration.teleport.teleport_request_duration_in_seconds")));
    }

    public Duration getTotemCooldown() {
        return Duration.ofSeconds(Objects.requireNonNull(getLong("configuration.totem_usage_cooldown_in_seconds")));
    }

    public Integer getDefaultHomeNumber() {
        return getInt("configuration.homes.default_number_of_homes");
    }

    public Long getHomeReplacementTimeout() {
        return getLong("configuration.homes.replace_home_timeout_ticks");
    }

    public Map<String, Integer> getHomePermissionGroups() {
        return get("configuration.homes.home_number_permission_groups");
    }

    public Integer getMobCombatDurationTicks() {
        return getInt("configuration.combat.mob_combat_duration_ticks");
    }

    public Integer getPlayerCombatDurationTicks() {
        return getInt("configuration.combat.player_combat_duration_ticks");
    }

    public Boolean isCombatLogOffDestroysTotemFirst() {
        return get("configuration.combat.should_log_off_destroy_totem_first");
    }

    public List<String> getCombatBlockedCommands() {
        return get("configuration.combat.blocked_commands");
    }

    public Boolean isHardcoreModeEnabled() {
        return get("configuration.combat.hardcore.enabled");
    }

    public Long getHardcoreDefaultBanTime() {
        return getLong("configuration.combat.hardcore.default_ban_time_in_seconds");
    }

    public Map<String, Integer> getHardcoreBanTimeGroups() {
        return get("configuration.combat.hardcore.permission_ban_time_groups");
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(Objects.requireNonNull(get("language.date_time_format")));
    }

    public Component getPlayersOnly() {
        return TextUtil.toComponent(get("language.players_only")).build();
    }

    public Component getCombatBarTitle() {
        return TextUtil.toComponent(get("language.combat.bar_title")).build();
    }

    public Component getCombatBlockedCommand() {
        return TextUtil.toComponent(get("language.combat.command_is_blocked_while_in_combat")).build();
    }

    public String getUnknownWorldName() {
        return get("language.combat.unknown_world_name");
    }

    public Component getCombatDeathLocation(@NotNull Location location) {
        String worldName = getUnknownWorldName();
        World world = location.getWorld();
        if (world != null) {
            worldName = world.getName().replaceAll("_", " ");
        }
        return TextUtil.toComponent(get("language.combat.your_death_location_is"))
                       .replace("%world%", worldName)
                       .replace("%x%", String.valueOf(location.getBlockX()))
                       .replace("%y%", String.valueOf(location.getBlockY()))
                       .replace("%z%", String.valueOf(location.getBlockZ()))
                       .build();
    }

    public Component getHardcoreBanMessage(@NotNull ZonedDateTime expirationDate) {
        return TextUtil.toComponent(get("language.combat.hardcore.banned"))
                       .replace("%expiration_date%", getDateTimeFormatter().format(expirationDate))
                       .build();
    }

    public Component getTeleportBarTitle() {
        return TextUtil.toComponent(get("language.teleport.bar_title")).build();
    }

    public Component getTeleportPlayerQuit() {
        return TextUtil.toComponent(get("language.teleport.player_quit")).build();
    }

    public Component getTeleportPlayerInCombat() {
        return TextUtil.toComponent(get("language.teleport.entered_combat")).build();
    }

    public Component getTeleportParticipantTeleporting() {
        return TextUtil.toComponent(get("language.teleport.other_player_is_teleporting")).build();
    }

    public Component getTeleportParticipantInCombat() {
        return TextUtil.toComponent(get("language.teleport.other_player_is_in_combat")).build();
    }

    public Component getTeleportAlreadyTeleporting() {
        return TextUtil.toComponent(get("language.teleport.already_teleporting")).build();
    }

    public Component getTeleportDestinationUnavailable() {
        return TextUtil.toComponent(get("language.teleport.destination_unavailable")).build();
    }

    public Component getTeleportInCooldown(long cooldownSeconds) {
        String messageFormat = get("language.teleport.in_cooldown");
        return TextUtil.toComponent(messageFormat).replace("%cooldown%", String.valueOf(cooldownSeconds)).build();
    }

    public Component getTeleportFailed() {
        return TextUtil.toComponent(get("language.teleport.failed")).build();
    }

    public Component getTeleportNoBackLocation() {
        return TextUtil.toComponent(get("language.teleport.back.no_back_location")).build();
    }

    public Component getTeleportPlayerNotFound() {
        return TextUtil.toComponent(get("language.teleport.player_not_found")).build();
    }

    public Component getTeleportHelp() {
        return TextUtil.toComponent(get("language.teleport.help")).build();
    }

    public Component getTeleportRequestReceived(@NotNull String username) {
        return TextUtil.toComponent(get("language.teleport.requests.teleport_received"))
                       .replace("%username%", username)
                       .build();
    }

    public Component getTeleportHereRequestReceived(@NotNull String username) {
        return TextUtil.toComponent(get("language.teleport.requests.teleport_here_received"))
                       .replace("%username%", username)
                       .build();
    }

    public Component getTeleportRequestSent(@NotNull String username) {
        return TextUtil.toComponent(get("language.teleport.requests.sent")).replace("%username%", username).build();
    }

    public Component getTeleportRequestNotUpdated(@NotNull String username) {
        return TextUtil.toComponent(get("language.teleport.requests.not_updated"))
                       .replace("%username%", username)
                       .build();
    }

    public Component getTeleportRequestNotFound() {
        return TextUtil.toComponent(get("language.teleport.requests.no_found")).build();
    }

    public Component getTeleportRequestCancelled() {
        return TextUtil.toComponent(get("language.teleport.requests.request_cancelled")).build();
    }

    public Component getTeleportRequestManyFound(@NotNull Collection<TeleportRequest> requests) {
        return TextUtil.toComponent(get("language.teleport.requests.many_found"))
                       .replace("%list%",
                                TextUtil.joinStrings(requests,
                                                     ", ",
                                                     request -> request.getRequester().getPlayer().getName()))
                       .build();
    }

    public Component getTeleportRequestsSentCancelled() {
        return TextUtil.toComponent(get("language.teleport.requests.sent_requests_cancelled")).build();
    }

    public Component getTeleportHomeHelp() {
        return TextUtil.toComponent(get("language.teleport.homes.help")).build();

    }

    public Component getTeleportHomeList(@NotNull List<Home> homes) {
        return TextUtil.toComponent(get("language.teleport.homes.list"))
                       .replace("%list%", TextUtil.joinStrings(homes, ", ", Home::getName))
                       .build();
    }

    public Component getTeleportHomeMaxCapacity() {
        return TextUtil.toComponent(get("language.teleport.homes.at_max_capacity")).build();
    }

    public Component getTeleportHomeCreateHelp() {
        return TextUtil.toComponent(get("language.teleport.homes.create_help")).build();
    }

    public Component getTeleportHomeCreated() {
        return TextUtil.toComponent(get("language.teleport.homes.created")).build();
    }

    public Component getTeleportHomeAlreadyExists() {
        return TextUtil.toComponent(get("language.teleport.homes.already_exists")).build();
    }

    public Component getTeleportHomeNotFound() {
        return TextUtil.toComponent(get("language.teleport.homes.not_found")).build();
    }

    public Component getTeleportHomeDeleted() {
        return TextUtil.toComponent(get("language.teleport.homes.deleted")).build();
    }

    public Component getTeleportHomeDeleteHelp(@NotNull Collection<Home> homes) {
        return TextUtil.toComponent(get("language.teleport.homes.delete_help"))
                       .replace("%list%", TextUtil.joinStrings(homes, ", ", Home::getName))
                       .build();
    }

    public Component getTeleportWarpManageHelp() {
        return TextUtil.toComponent(get("language.teleport.warps.manage.help")).build();
    }

    public Component getTeleportWarpManageSuccess() {
        return TextUtil.toComponent(get("language.teleport.warps.manage.success")).build();
    }

    public Component getTeleportWarpManageDatabaseFailure() {
        return TextUtil.toComponent(get("language.teleport.warps.manage.failure")).build();
    }

    public Component getTeleportWarpFailedToRetrieve() {
        return TextUtil.toComponent(get("language.teleport.warps.failed_to_retrieve")).build();
    }

    public Component getTeleportWarpList(@NotNull Collection<Warp> warps) {
        return TextUtil.toComponent(get("language.teleport.warps.list"))
                       .replace("%list%", TextUtil.joinStrings(warps, ", ", Warp::getName))
                       .build();
    }

    public Component getTeleportWarpNotFound() {
        return TextUtil.toComponent(get("language.teleport.warps.not_found")).build();
    }

    public Component getTeleportWorldList(@NotNull Collection<World> worlds) {
        return TextUtil.toComponent(get("language.teleport.worlds.list"))
                       .replace("%list%", TextUtil.joinStrings(worlds, ", ", World::getName))
                       .build();
    }

    public Component getTeleportWorldNotFound() {
        return TextUtil.toComponent(get("language.teleport.worlds.not_found")).build();
    }

    public Component getTotemInCooldown() {
        return TextUtil.toComponent(get("language.totem.totem_in_cooldown")).build();
    }

    public Component getTotemEnteredCooldown() {
        return TextUtil.toComponent(get("language.totem.totem_entered_in_cooldown")).build();
    }

    public Component getKickMessageCouldNotLoadProfile() {
        return TextUtil.toComponent(get("language.kick_messages.could_not_load_profile")).build();
    }
}
