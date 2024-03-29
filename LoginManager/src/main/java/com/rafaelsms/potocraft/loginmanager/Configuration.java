package com.rafaelsms.potocraft.loginmanager;

import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
        return getOrThrow("configuration.database.mongo_uri");
    }

    public String getMongoDatabase() {
        return getOrThrow("configuration.database.mongo_database");
    }

    public String getMongoPlayerProfileCollection() {
        return getOrThrow("configuration.database.player_profile_collection");
    }

    public List<String> getAllowedCommandsLoggedOff() {
        return getOrThrow("configuration.offline_players.offline_allowed_commands");
    }

    public List<String> getBlockedCommandsMuted() {
        return getOrThrow("configuration.muted_blocked_commands");
    }

    public Pattern getAllowedJavaUsernamesRegex() {
        return Pattern.compile(Objects.requireNonNull(getOrThrow("configuration.allowed_java_usernames_regex")),
                               Pattern.CASE_INSENSITIVE);
    }

    public String getLoginServer() {
        return getOrThrow("configuration.offline_players.offline_players_login_server_name");
    }

    public String getDefaultServer() {
        return getOrThrow("configuration.default_server_name");
    }

    public Duration getAutoLoginWindow() {
        return Duration.ofMinutes(Objects.requireNonNull(getLongOrNull(
                "configuration.offline_players.minutes_between_joins_to_auto_login")));
    }

    public Integer getMaxAccountsPerAddress() {
        return getIntOrNull("configuration.offline_players.max_accounts_registered_per_address");
    }

    public @NotNull DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(Objects.requireNonNull(getOrThrow("language.generic.date_time_formatter")));
    }

    public @NotNull BaseComponent[] getNoPermission() {
        return TextUtil.toComponentBungee(getOrThrow("language.generic.no_permission"));
    }

    public @NotNull BaseComponent[] getNoPlayerFound() {
        return TextUtil.toComponentBungee(getOrThrow("language.generic.no_player_found"));
    }

    public @NotNull BaseComponent[] getCommandPlayersOnly() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.players_only"));
    }

    public @NotNull BaseComponent[] getCommandOfflinePlayersOnly() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.offline_players_only"));
    }

    public @NotNull BaseComponent[] getCommandIncorrectPassword() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.incorrect_password"));
    }

    public @NotNull BaseComponent[] getCommandIncorrectPasswordFormat() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.incorrect_password_format"));
    }

    public @NotNull BaseComponent[] getCommandLoggedInOnly() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.logged_in_players_only"));
    }

    public @NotNull BaseComponent[] getCommandLoggedIn() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.logged_in"));
    }

    public @NotNull BaseComponent[] getCommandFailedToSearchProfile() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.failed_to_search_profile"));
    }

    public @NotNull BaseComponent[] getCommandNoProfileFound() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.no_profile_found"));
    }

    public @NotNull BaseComponent[] getCommandFailedToSaveProfile() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.failed_to_save_profile"));
    }

    public @NotNull BaseComponent[] getCommandChangePasswordHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.change_password.help"));
    }

    public @NotNull BaseComponent[] getCommandChangePasswordDoNotMatch() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.change_password.passwords_do_not_match"));
    }

    public @NotNull BaseComponent[] getCommandChangePasswordRegisterFirst() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.change_password.register_instead"));
    }

    public @NotNull BaseComponent[] getCommandChangedPasswordSuccessful() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.change_password.success"));
    }

    public @NotNull BaseComponent[] getCommandRegisterHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.register.help"));
    }

    public @NotNull BaseComponent[] getCommandRegisterPasswordsDoNotMatch() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.register.passwords_do_not_match"));
    }

    public @NotNull BaseComponent[] getCommandRegisterAccountLimitForAddress() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.register.max_accounts_per_address_reached"));
    }

    public @NotNull BaseComponent[] getCommandRegisterShouldChangePasswordInstead() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.register.change_password_instead"));
    }

    public @NotNull BaseComponent[] getCommandRegisterShouldLoginInstead() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.register.login_instead"));
    }

    public @NotNull BaseComponent[] getCommandLoginHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.login.help"));
    }

    public @NotNull BaseComponent[] getCommandLoginRegisterFirst() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.login.register_instead"));
    }

    public @NotNull BaseComponent[] getCommandLoginAlreadyLoggedIn() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.login.already_logged_in"));
    }

    public @NotNull BaseComponent[] getCommandNoServerAvailable() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.login.no_server_available"));
    }

    public @NotNull BaseComponent[] getCommandUnbanHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.unban.help"));
    }

    public @NotNull BaseComponent[] getCommandUnpunished(@NotNull String playerName) {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.player_unpunished"),
                                          Placeholder.unparsed("player", playerName));
    }

    public @NotNull BaseComponent[] getCommandPlayerIsNotPunished(@NotNull String playerName) {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.player_is_not_punished"),
                                          Placeholder.unparsed("player", playerName));
    }

    public @NotNull BaseComponent[] getCommandBanHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.ban.help"));
    }

    public @NotNull BaseComponent[] getCommandBanPlayerOffline() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.ban.player_offline"));
    }

    public @NotNull BaseComponent[] getPlayerPunished(@NotNull String playerName) {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.player_punished"),
                                          Placeholder.unparsed("player", playerName));
    }

    public @NotNull BaseComponent[] getListServerList(@NotNull String serverName,
                                                      @NotNull Collection<ProxiedPlayer> playerList) {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.list_server_players"),
                                          Placeholder.unparsed("server_name", serverName),
                                          Placeholder.unparsed("size", String.valueOf(playerList.size())),
                                          Placeholder.unparsed("player_list",
                                                               TextUtil.joinStrings(playerList,
                                                                                    ", ",
                                                                                    ProxiedPlayer::getName)));
    }

    public @NotNull BaseComponent[] getCommandSeenHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.seen.help"));
    }

    private Component getCommandSeenReportEntries(@NotNull LoginManagerPlugin plugin,
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

            lines.add(TextUtil.toComponent(getOrThrow("language.commands.seen.report_entry"),
                                           Placeholder.unparsed("reporter_id",
                                                                Util.convertFallback(entry.getReporterId(),
                                                                                     UUID::toString,
                                                                                     "console")),
                                           Placeholder.unparsed("reporter_name", reporterName),
                                           Placeholder.unparsed("type", entry.getType()),
                                           Placeholder.unparsed("active", entry.isActive() ? "enabled" : "disabled"),
                                           Placeholder.unparsed("date",
                                                                Util.convertNonNull(entry.getDate(),
                                                                                    getDateTimeFormatter()::format)),
                                           Placeholder.unparsed("expiration_date",
                                                                Util.convertFallback(entry.getExpirationDate(),
                                                                                     getDateTimeFormatter()::format,
                                                                                     "?")),
                                           Placeholder.unparsed("reason", Util.getOrElse(entry.getReason(), "?"))));
        }
        return Component.join(JoinConfiguration.builder().separator(Component.newline()).build(), lines);
    }

    public @NotNull BaseComponent[] getCommandSeen(@NotNull LoginManagerPlugin plugin, @NotNull Profile profile) {
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
        return TextUtil.toComponentBungee(getOrThrow("language.commands.seen.profile"),
                                          Placeholder.parsed("user_name", profile.getLastPlayerName()),
                                          Placeholder.parsed("user_id", profile.getPlayerId().toString()),
                                          Placeholder.parsed("user_ip", profile.getLastAddress().orElse("?")),
                                          Placeholder.unparsed("user_has_password",
                                                               profile.hasPassword() ? "yes" : "no"),
                                          Placeholder.unparsed("server_name", profile.getLastServerName().orElse("?")),
                                          Placeholder.unparsed("play_time", playTime),
                                          Placeholder.unparsed("join_date", lastJoinDate),
                                          Placeholder.unparsed("quit_date", lastQuitDate),
                                          Placeholder.component("report_entries",
                                                                getCommandSeenReportEntries(plugin,
                                                                                            profile.getReportEntries())));
    }

    public @NotNull BaseComponent[] getCommandTemporaryBanHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.temporary_ban.help"));
    }

    public @NotNull BaseComponent[] getCommandTemporaryBanPlayerOffline() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.temporary_ban.player_offline"));
    }

    public @NotNull BaseComponent[] getCommandUnmuteHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.unmute.help"));
    }

    public @NotNull BaseComponent[] getCommandMuteHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.mute.help"));
    }

    public @NotNull BaseComponent[] getCommandMutePlayerOffline() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.mute.player_offline"));
    }

    public @NotNull BaseComponent[] getCommandKickHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.kick.help"));
    }

    private String formatPing(int ping) {
        return String.valueOf(Math.min(ping, 999));
    }

    public @NotNull BaseComponent[] getCommandPing(int ping, int averagePing) {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.ping.self_ping"),
                                          Placeholder.parsed("ping", formatPing(ping)),
                                          Placeholder.parsed("average", formatPing(averagePing)));
    }

    public @NotNull BaseComponent[] getCommandPingOthers(@NotNull String playerName, int ping, int averagePing) {
        return TextUtil.toComponentBungee(getOrThrow("language.commands.ping.other_player_ping"),
                                          Placeholder.parsed("username", playerName),
                                          Placeholder.parsed("ping", formatPing(ping)),
                                          Placeholder.parsed("average", formatPing(averagePing)));
    }

    public @NotNull BaseComponent[] getKickMessageCouldNotCheckMojangUsername() {
        return TextUtil.toComponentBungee(getOrThrow("language.kick_messages.could_not_check_mojang"));
    }

    public @NotNull BaseComponent[] getKickMessageInvalidPrefixForJavaPlayer() {
        return TextUtil.toComponentBungee(getOrThrow("language.kick_messages.invalid_java_prefix"));
    }

    public @NotNull BaseComponent[] getKickMessageInvalidJavaUsername() {
        return TextUtil.toComponentBungee(getOrThrow("language.kick_messages.invalid_java_username"));
    }

    public @NotNull BaseComponent[] getKickMessageFailedToRetrieveProfile() {
        return TextUtil.toComponentBungee(getOrThrow("language.kick_messages.failed_to_retrieve_player_profile"));
    }

    public @NotNull BaseComponent[] getKickMessageFailedToSaveProfile() {
        return TextUtil.toComponentBungee(getOrThrow("language.kick_messages.failed_to_save_player_profile"));
    }

    public @NotNull BaseComponent[] getKickMessageLoginServerUnavailable() {
        return TextUtil.toComponentBungee(getOrThrow("language.kick_messages.login_server_unavailable"));
    }

    private @NotNull BaseComponent[] getPunishmentMessage(@Nullable String baseMessage,
                                                          @Nullable String reporterName,
                                                          @Nullable String reason,
                                                          @Nullable String expirationDate) {
        String reporterFallback = Objects.requireNonNull(getOrThrow("language.generic.console_name"));
        String reasonFallback = Objects.requireNonNull(getOrThrow("language.generic.report_reason_unknown"));
        String expirationDateFallback = Objects.requireNonNull(getOrThrow("language.generic.no_expiration_date"));
        return TextUtil.toComponentBungee(baseMessage,
                                          Placeholder.unparsed("reporter",
                                                               Util.getOrElse(reporterName, reporterFallback)),
                                          Placeholder.unparsed("reason", Util.getOrElse(reason, reasonFallback)),
                                          Placeholder.unparsed("expiration_date",
                                                               Util.getOrElse(expirationDate, expirationDateFallback)));
    }

    public @NotNull BaseComponent[] getPunishmentMessageBanned(@Nullable String reporterName,
                                                               @Nullable ZonedDateTime expirationDate,
                                                               @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(getOrThrow("language.punishment.banned"),
                                    reporterName,
                                    reason,
                                    expirationDateString);
    }

    public @NotNull BaseComponent[] getPunishmentMessageMuted(@Nullable String reporterName,
                                                              @Nullable ZonedDateTime expirationDate,
                                                              @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(getOrThrow("language.punishment.muted"),
                                    reporterName,
                                    reason,
                                    expirationDateString);
    }

    public @NotNull BaseComponent[] getPunishmentMessageBlockedCommandMuted() {
        return TextUtil.toComponentBungee(getOrThrow("language.punishment.commands_muted"));
    }

    public @NotNull BaseComponent[] getPunishmentMessageKicked(@Nullable String reporterName, @Nullable String reason) {
        return getPunishmentMessage(getOrThrow("language.punishment.kicked"), reporterName, reason, null);
    }

    public @NotNull BaseComponent[] getPunishmentMessageLoggedOff() {
        return TextUtil.toComponentBungee(getOrThrow("language.punishment.logged_off"));
    }
}
