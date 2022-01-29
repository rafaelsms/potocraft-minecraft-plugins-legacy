package com.rafaelsms.potocraft.loginmanager;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class Configuration extends YamlFile {

    public Configuration(@NotNull Path dataDirectory) throws IOException {
        super(dataDirectory.toFile(), "config.yml");
    }

    public String getMongoURI() {
        return get("configuration.database.mongo_uri");
    }

    public String getMongoDatabase() {
        return get("configuration.database.mongo_database");
    }

    public String getMongoPlayerProfileCollection() {
        return get("configuration.database.player_profile_collection");
    }

    public Boolean isMongoDatabaseFailureFatal() {
        return get("configuration.database.is_exception_fatal");
    }

    public List<String> getAllowedCommandsLoggedOff() {
        return get("configuration.offline_players.offline_allowed_commands");
    }

    public List<String> getBlockedCommandsMuted() {
        return get("configuration.muted_blocked_commands");
    }

    public Pattern getAllowedUsernamesRegex() {
        return Pattern.compile(Objects.requireNonNull(get("configuration.allowed_usernames_regex")),
                               Pattern.CASE_INSENSITIVE);
    }

    public String getLoginServer() {
        return get("configuration.offline_players.offline_players_login_server");
    }

    public Duration getAutoLoginWindow() {
        return Duration.ofMinutes(Objects.requireNonNull(getLong(
                "configuration.offline_players.minutes_between_joins_to_auto_login")));
    }

    public Integer getMaxAccountsPerAddress() {
        return getInt("configuration.offline_players.max_accounts_registered_per_address");
    }

    public Component getTabDisplayName(@NotNull Player player, @NotNull String playerServer) {
        return TextUtil.toComponent(get("configuration.tab_list_other_server_entry_format"))
                       .replace("%server_name%", playerServer)
                       .replace("%prefix%", TextUtil.getPrefix(player.getUniqueId()))
                       .replace("%player_name%", player.getUsername())
                       .replace("%suffix%", TextUtil.getSuffix(player.getUniqueId()))
                       .build();
    }

    public @NotNull DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(Objects.requireNonNull(get("language.generic.date_time_formatter")));
    }

    public Component getNoPermission() {
        return TextUtil.toComponent(get("language.generic.no_permission")).build();
    }

    public Component getNoPlayerFound() {
        return TextUtil.toComponent(get("language.generic.no_player_found")).build();
    }

    public Component getCommandPlayersOnly() {
        return TextUtil.toComponent(get("language.commands.players_only")).build();
    }

    public Component getCommandOfflinePlayersOnly() {
        return TextUtil.toComponent(get("language.commands.offline_players_only")).build();
    }

    public Component getCommandIncorrectPin() {
        return TextUtil.toComponent(get("language.commands.incorrect_pin")).build();
    }

    public Component getCommandIncorrectPinFormat() {
        return TextUtil.toComponent(get("language.commands.incorrect_pin_format")).build();
    }

    public Component getCommandLoggedInOnly() {
        return TextUtil.toComponent(get("language.commands.logged_in_players_only")).build();
    }

    public Component getCommandLoggedIn() {
        return TextUtil.toComponent(get("language.commands.logged_in")).build();
    }

    public Component getCommandFailedToSearchProfile() {
        return TextUtil.toComponent(get("language.commands.failed_to_search_profile")).build();
    }

    public Component getCommandNoProfileFound() {
        return TextUtil.toComponent(get("language.commands.no_profile_found")).build();
    }

    public Component getCommandFailedToSaveProfile() {
        return TextUtil.toComponent(get("language.commands.failed_to_save_profile")).build();
    }

    public Component getCommandChangePinHelp() {
        return TextUtil.toComponent(get("language.commands.change_pin.help")).build();
    }

    public Component getCommandChangePinInvalidPins() {
        return TextUtil.toComponent(get("language.commands.change_pin.invalid_pins")).build();
    }

    public Component getCommandChangePinRegisterFirst() {
        return TextUtil.toComponent(get("language.commands.change_pin.register_instead")).build();
    }

    public Component getCommandChangedPinSuccessful() {
        return TextUtil.toComponent(get("language.commands.change_pin.success")).build();
    }

    public Component getCommandRegisterHelp() {
        return TextUtil.toComponent(get("language.commands.register.help")).build();
    }

    public Component getCommandRegisterInvalidPin() {
        return TextUtil.toComponent(get("language.commands.register.invalid_pins")).build();
    }

    public Component getCommandRegisterAccountLimitForAddress() {
        return TextUtil.toComponent(get("language.commands.register.max_accounts_per_address_reached")).build();
    }

    public Component getCommandRegisterShouldChangePinInstead() {
        return TextUtil.toComponent(get("language.commands.register.change_pin_instead")).build();
    }

    public Component getCommandRegisterShouldLoginInstead() {
        return TextUtil.toComponent(get("language.commands.register.login_instead")).build();
    }

    public Component getCommandLoginHelp() {
        return TextUtil.toComponent(get("language.commands.login.help")).build();
    }

    public Component getCommandLoginRegisterFirst() {
        return TextUtil.toComponent(get("language.commands.login.register_instead")).build();
    }

    public Component getCommandLoginAlreadyLoggedIn() {
        return TextUtil.toComponent(get("language.commands.login.already_logged_in")).build();
    }

    public Component getCommandLoginNoServerAvailable() {
        return TextUtil.toComponent(get("language.commands.login.no_server_available")).build();
    }

    public Component getCommandUnbanHelp() {
        return TextUtil.toComponent(get("language.commands.unban.help")).build();
    }

    public Component getCommandUnpunished(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.commands.player_unpunished")).replace("%player%", playerName).build();
    }

    public Component getCommandPlayerIsNotPunished(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.commands.player_is_not_punished"))
                       .replace("%player%", playerName)
                       .build();
    }

    public Component getCommandBanHelp() {
        return TextUtil.toComponent(get("language.commands.ban.help")).build();
    }

    public Component getCommandBanPlayerOffline() {
        return TextUtil.toComponent(get("language.commands.ban.player_offline")).build();
    }

    public Component getPlayerPunished(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.commands.player_punished")).replace("%player%", playerName).build();
    }

    public Component getListServerList(@NotNull String serverName, @NotNull Collection<Player> playerList) {
        return TextUtil.toComponent(get("language.commands.list_server_players"))
                       .replace("%server_name%", serverName)
                       .replace("%size%", String.valueOf(playerList.size()))
                       .replace("%player_list%", TextUtil.joinStrings(playerList, ", ", Player::getUsername))
                       .build();
    }

    public Component getCommandSeenHelp() {
        return TextUtil.toComponent(get("language.commands.seen.help")).build();
    }

    public Component getCommandSeenReportEntries(@NotNull LoginManagerPlugin plugin,
                                                 @NotNull Collection<ReportEntry> reportEntries) {
        List<Component> lines = new ArrayList<>(reportEntries.size());
        for (ReportEntry entry : reportEntries) {
            String reporterName = "console";
            if (entry.getReporterId() != null) {
                Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(entry.getReporterId());
                if (optionalProfile.isPresent()) {
                    reporterName = optionalProfile.get().getLastPlayerName();
                }
            }

            lines.add(TextUtil.toComponent(get("language.commands.seen.report_entry"))
                              .replace("%reporter_id%",
                                       Util.convertFallback(entry.getReporterId(), UUID::toString, "console"))
                              .replace("%reporter_name%", reporterName)
                              .replace("%type%", entry.getType())
                              .replace("%active%", entry.isActive() ? "enabled" : "disabled")
                              .replace("%expiration_date%",
                                       Util.convertFallback(entry.getExpirationDate(), getDateTimeFormatter()::format,
                                                            "?"))
                              .replace("%reason%", Util.getOrElse(entry.getReason(), "?"))
                              .build());
        }
        return Component.join(Component.newline(), lines);
    }

    public Component getCommandSeen(@NotNull LoginManagerPlugin plugin, @NotNull Profile profile) {
        Duration playTimeDuration = Duration.ofMillis(profile.getPlayTime().orElse(0L));
        String playTime = playTimeDuration.toDaysPart() +
                          "d" +
                          playTimeDuration.toHoursPart() +
                          "h" +
                          playTimeDuration.toMinutesPart() +
                          "m" +
                          playTimeDuration.toSecondsPart() +
                          "s";
        String lastJoinDate =
                Util.convertFallback(profile.getLastJoinDate().orElse(null), getDateTimeFormatter()::format, "?");
        String lastQuitDate =
                Util.convertFallback(profile.getLastQuitDate().orElse(null), getDateTimeFormatter()::format, "?");
        return TextUtil.toComponent(get("language.commands.seen.profile"))
                       .replace("%user_name%", profile.getLastPlayerName())
                       .replace("%user_id%", profile.getPlayerId().toString())
                       .replace("%server_name%", profile.getLastServerName().orElse("?"))
                       .replace("%play_time%", playTime)
                       .replace("%join_date%", lastJoinDate)
                       .replace("%quit_date%", lastQuitDate)
                       .replace("%report_entries%", getCommandSeenReportEntries(plugin, profile.getReportEntries()))
                       .build();
    }

    public Component getCommandTemporaryBanHelp() {
        return TextUtil.toComponent(get("language.commands.temporary_ban.help")).build();
    }

    public Component getCommandTemporaryBanPlayerOffline() {
        return TextUtil.toComponent(get("language.commands.temporary_ban.player_offline")).build();
    }

    public Component getCommandUnmuteHelp() {
        return TextUtil.toComponent(get("language.commands.unmute.help")).build();
    }

    public Component getCommandMuteHelp() {
        return TextUtil.toComponent(get("language.commands.mute.help")).build();
    }

    public Component getCommandMutePlayerOffline() {
        return TextUtil.toComponent(get("language.commands.mute.player_offline")).build();
    }

    public Component getCommandKickHelp() {
        return TextUtil.toComponent(get("language.commands.kick.help")).build();
    }

    public Component getKickMessageCouldNotCheckMojangUsername() {
        return TextUtil.toComponent(get("language.kick_messages.could_not_check_mojang")).build();
    }

    public Component getKickMessageInvalidPrefixForJavaPlayer() {
        return TextUtil.toComponent(get("language.kick_messages.invalid_java_prefix")).build();
    }

    public Component getKickMessageInvalidUsername() {
        return TextUtil.toComponent(get("language.kick_messages.invalid_username")).build();
    }

    public Component getKickMessageFailedToRetrieveProfile() {
        return TextUtil.toComponent(get("language.kick_messages.failed_to_retrieve_player_profile")).build();
    }

    public Component getKickMessageFailedToSaveProfile() {
        return TextUtil.toComponent(get("language.kick_messages.failed_to_save_player_profile")).build();
    }

    public Component getKickMessageLoginServerUnavailable() {
        return TextUtil.toComponent(get("language.kick_messages.login_server_unavailable")).build();
    }

    private Component getPunishmentMessage(@Nullable String baseMessage,
                                           @Nullable String reporterName,
                                           @Nullable String reason,
                                           @Nullable String expirationDate) {
        String reporterFallback = Objects.requireNonNull(get("language.generic.console_name"));
        String reasonFallback = Objects.requireNonNull(get("language.generic.report_reason_unknown"));
        String expirationDateFallback = Objects.requireNonNull(get("language.generic.no_expiration_date"));
        return TextUtil.toComponent(baseMessage)
                       .replace("%reporter%", Util.getOrElse(reporterName, reporterFallback))
                       .replace("%reason%", Util.getOrElse(reason, reasonFallback))
                       .replace("%expiration_date%", Util.getOrElse(expirationDate, expirationDateFallback))
                       .build();
    }

    public Component getPunishmentMessageBanned(@Nullable String reporterName,
                                                @Nullable ZonedDateTime expirationDate,
                                                @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(get("language.punishment.banned"), reporterName, reason, expirationDateString);
    }

    public Component getPunishmentMessageMuted(@Nullable String reporterName,
                                               @Nullable ZonedDateTime expirationDate,
                                               @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(get("language.punishment.muted"), reporterName, reason, expirationDateString);
    }

    public Component getPunishmentMessageBlockedCommandMuted() {
        return TextUtil.toComponent(get("language.punishment.commands_muted")).build();
    }

    public Component getPunishmentMessageKicked(@Nullable String reporterName, @Nullable String reason) {
        return getPunishmentMessage(get("language.punishment.kicked"), reporterName, reason, null);
    }

    public Component getPunishmentMessageLoggedOff() {
        return TextUtil.toComponent(get("language.punishment.logged_off")).build();
    }
}
