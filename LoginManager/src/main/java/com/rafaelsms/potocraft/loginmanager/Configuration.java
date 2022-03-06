package com.rafaelsms.potocraft.loginmanager;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.Template;
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
        return get("configuration.database.mongo_uri");
    }

    public String getMongoDatabase() {
        return get("configuration.database.mongo_database");
    }

    public String getMongoPlayerProfileCollection() {
        return get("configuration.database.player_profile_collection");
    }

    public List<String> getAllowedCommandsLoggedOff() {
        return get("configuration.offline_players.offline_allowed_commands");
    }

    public List<String> getBlockedCommandsMuted() {
        return get("configuration.muted_blocked_commands");
    }

    public Pattern getAllowedJavaUsernamesRegex() {
        return Pattern.compile(Objects.requireNonNull(get("configuration.allowed_java_usernames_regex")),
                               Pattern.CASE_INSENSITIVE);
    }

    public String getLoginServer() {
        return get("configuration.offline_players.offline_players_login_server_name");
    }

    public String getDefaultServer() {
        return get("configuration.default_server_name");
    }

    public Duration getAutoLoginWindow() {
        return Duration.ofMinutes(Objects.requireNonNull(getLong(
                "configuration.offline_players.minutes_between_joins_to_auto_login")));
    }

    public Integer getMaxAccountsPerAddress() {
        return getInt("configuration.offline_players.max_accounts_registered_per_address");
    }

    public @NotNull DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(Objects.requireNonNull(get("language.generic.date_time_formatter")));
    }

    public @NotNull BaseComponent[] getNoPermission() {
        return TextUtil.toComponentBungee(get("language.generic.no_permission"));
    }

    public @NotNull BaseComponent[] getNoPlayerFound() {
        return TextUtil.toComponentBungee(get("language.generic.no_player_found"));
    }

    public @NotNull BaseComponent[] getCommandPlayersOnly() {
        return TextUtil.toComponentBungee(get("language.commands.players_only"));
    }

    public @NotNull BaseComponent[] getCommandOfflinePlayersOnly() {
        return TextUtil.toComponentBungee(get("language.commands.offline_players_only"));
    }

    public @NotNull BaseComponent[] getCommandIncorrectPin() {
        return TextUtil.toComponentBungee(get("language.commands.incorrect_pin"));
    }

    public @NotNull BaseComponent[] getCommandIncorrectPinFormat() {
        return TextUtil.toComponentBungee(get("language.commands.incorrect_pin_format"));
    }

    public @NotNull BaseComponent[] getCommandLoggedInOnly() {
        return TextUtil.toComponentBungee(get("language.commands.logged_in_players_only"));
    }

    public @NotNull BaseComponent[] getCommandLoggedIn() {
        return TextUtil.toComponentBungee(get("language.commands.logged_in"));
    }

    public @NotNull BaseComponent[] getCommandFailedToSearchProfile() {
        return TextUtil.toComponentBungee(get("language.commands.failed_to_search_profile"));
    }

    public @NotNull BaseComponent[] getCommandNoProfileFound() {
        return TextUtil.toComponentBungee(get("language.commands.no_profile_found"));
    }

    public @NotNull BaseComponent[] getCommandFailedToSaveProfile() {
        return TextUtil.toComponentBungee(get("language.commands.failed_to_save_profile"));
    }

    public @NotNull BaseComponent[] getCommandChangePinHelp() {
        return TextUtil.toComponentBungee(get("language.commands.change_pin.help"));
    }

    public @NotNull BaseComponent[] getCommandChangePinInvalidPins() {
        return TextUtil.toComponentBungee(get("language.commands.change_pin.invalid_pins"));
    }

    public @NotNull BaseComponent[] getCommandChangePinRegisterFirst() {
        return TextUtil.toComponentBungee(get("language.commands.change_pin.register_instead"));
    }

    public @NotNull BaseComponent[] getCommandChangedPinSuccessful() {
        return TextUtil.toComponentBungee(get("language.commands.change_pin.success"));
    }

    public @NotNull BaseComponent[] getCommandRegisterHelp() {
        return TextUtil.toComponentBungee(get("language.commands.register.help"));
    }

    public @NotNull BaseComponent[] getCommandRegisterInvalidPin() {
        return TextUtil.toComponentBungee(get("language.commands.register.invalid_pins"));
    }

    public @NotNull BaseComponent[] getCommandRegisterAccountLimitForAddress() {
        return TextUtil.toComponentBungee(get("language.commands.register.max_accounts_per_address_reached"));
    }

    public @NotNull BaseComponent[] getCommandRegisterShouldChangePinInstead() {
        return TextUtil.toComponentBungee(get("language.commands.register.change_pin_instead"));
    }

    public @NotNull BaseComponent[] getCommandRegisterShouldLoginInstead() {
        return TextUtil.toComponentBungee(get("language.commands.register.login_instead"));
    }

    public @NotNull BaseComponent[] getCommandLoginHelp() {
        return TextUtil.toComponentBungee(get("language.commands.login.help"));
    }

    public @NotNull BaseComponent[] getCommandLoginRegisterFirst() {
        return TextUtil.toComponentBungee(get("language.commands.login.register_instead"));
    }

    public @NotNull BaseComponent[] getCommandLoginAlreadyLoggedIn() {
        return TextUtil.toComponentBungee(get("language.commands.login.already_logged_in"));
    }

    public @NotNull BaseComponent[] getCommandNoServerAvailable() {
        return TextUtil.toComponentBungee(get("language.commands.login.no_server_available"));
    }

    public @NotNull BaseComponent[] getCommandUnbanHelp() {
        return TextUtil.toComponentBungee(get("language.commands.unban.help"));
    }

    public @NotNull BaseComponent[] getCommandUnpunished(@NotNull String playerName) {
        return TextUtil.toComponentBungee(get("language.commands.player_unpunished"),
                                          Template.of("player", playerName));
    }

    public @NotNull BaseComponent[] getCommandPlayerIsNotPunished(@NotNull String playerName) {
        return TextUtil.toComponentBungee(get("language.commands.player_is_not_punished"),
                                          Template.of("player", playerName));
    }

    public @NotNull BaseComponent[] getCommandBanHelp() {
        return TextUtil.toComponentBungee(get("language.commands.ban.help"));
    }

    public @NotNull BaseComponent[] getCommandBanPlayerOffline() {
        return TextUtil.toComponentBungee(get("language.commands.ban.player_offline"));
    }

    public @NotNull BaseComponent[] getPlayerPunished(@NotNull String playerName) {
        return TextUtil.toComponentBungee(get("language.commands.player_punished"), Template.of("player", playerName));
    }

    public @NotNull BaseComponent[] getListServerList(@NotNull String serverName,
                                                      @NotNull Collection<ProxiedPlayer> playerList) {
        return TextUtil.toComponentBungee(get("language.commands.list_server_players"),
                                          Template.of("server_name", serverName),
                                          Template.of("size", String.valueOf(playerList.size())),
                                          Template.of("player_list",
                                                      TextUtil.joinStrings(playerList, ", ", ProxiedPlayer::getName)));
    }

    public @NotNull BaseComponent[] getCommandSeenHelp() {
        return TextUtil.toComponentBungee(get("language.commands.seen.help"));
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

            lines.add(TextUtil.toComponent(get("language.commands.seen.report_entry"),
                                           Template.of("reporter_id",
                                                       Util.convertFallback(entry.getReporterId(),
                                                                            UUID::toString,
                                                                            "console")),
                                           Template.of("reporter_name", reporterName),
                                           Template.of("type", entry.getType()),
                                           Template.of("active", entry.isActive() ? "enabled" : "disabled"),
                                           Template.of("date",
                                                       Util.convertNonNull(entry.getDate(),
                                                                           getDateTimeFormatter()::format)),
                                           Template.of("expiration_date",
                                                       Util.convertFallback(entry.getExpirationDate(),
                                                                            getDateTimeFormatter()::format,
                                                                            "?")),
                                           Template.of("reason", Util.getOrElse(entry.getReason(), "?"))));
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
        return TextUtil.toComponentBungee(get("language.commands.seen.profile"),
                                          Template.of("user_name", profile.getLastPlayerName()),
                                          Template.of("user_id", profile.getPlayerId().toString()),
                                          Template.of("user_ip", profile.getLastAddress().orElse("?")),
                                          Template.of("server_name", profile.getLastServerName().orElse("?")),
                                          Template.of("play_time", playTime),
                                          Template.of("join_date", lastJoinDate),
                                          Template.of("quit_date", lastQuitDate),
                                          Template.of("report_entries",
                                                      getCommandSeenReportEntries(plugin, profile.getReportEntries())));
    }

    public @NotNull BaseComponent[] getCommandTemporaryBanHelp() {
        return TextUtil.toComponentBungee(get("language.commands.temporary_ban.help"));
    }

    public @NotNull BaseComponent[] getCommandTemporaryBanPlayerOffline() {
        return TextUtil.toComponentBungee(get("language.commands.temporary_ban.player_offline"));
    }

    public @NotNull BaseComponent[] getCommandUnmuteHelp() {
        return TextUtil.toComponentBungee(get("language.commands.unmute.help"));
    }

    public @NotNull BaseComponent[] getCommandMuteHelp() {
        return TextUtil.toComponentBungee(get("language.commands.mute.help"));
    }

    public @NotNull BaseComponent[] getCommandMutePlayerOffline() {
        return TextUtil.toComponentBungee(get("language.commands.mute.player_offline"));
    }

    public @NotNull BaseComponent[] getCommandKickHelp() {
        return TextUtil.toComponentBungee(get("language.commands.kick.help"));
    }

    public @NotNull BaseComponent[] getKickMessageCouldNotCheckMojangUsername() {
        return TextUtil.toComponentBungee(get("language.kick_messages.could_not_check_mojang"));
    }

    public @NotNull BaseComponent[] getKickMessageInvalidPrefixForJavaPlayer() {
        return TextUtil.toComponentBungee(get("language.kick_messages.invalid_java_prefix"));
    }

    public @NotNull BaseComponent[] getKickMessageInvalidJavaUsername() {
        return TextUtil.toComponentBungee(get("language.kick_messages.invalid_java_username"));
    }

    public @NotNull BaseComponent[] getKickMessageFailedToRetrieveProfile() {
        return TextUtil.toComponentBungee(get("language.kick_messages.failed_to_retrieve_player_profile"));
    }

    public @NotNull BaseComponent[] getKickMessageFailedToSaveProfile() {
        return TextUtil.toComponentBungee(get("language.kick_messages.failed_to_save_player_profile"));
    }

    public @NotNull BaseComponent[] getKickMessageLoginServerUnavailable() {
        return TextUtil.toComponentBungee(get("language.kick_messages.login_server_unavailable"));
    }

    private @NotNull BaseComponent[] getPunishmentMessage(@Nullable String baseMessage,
                                                          @Nullable String reporterName,
                                                          @Nullable String reason,
                                                          @Nullable String expirationDate) {
        String reporterFallback = Objects.requireNonNull(get("language.generic.console_name"));
        String reasonFallback = Objects.requireNonNull(get("language.generic.report_reason_unknown"));
        String expirationDateFallback = Objects.requireNonNull(get("language.generic.no_expiration_date"));
        return TextUtil.toComponentBungee(baseMessage,
                                          Template.of("reporter", Util.getOrElse(reporterName, reporterFallback)),
                                          Template.of("reason", Util.getOrElse(reason, reasonFallback)),
                                          Template.of("expiration_date",
                                                      Util.getOrElse(expirationDate, expirationDateFallback)));
    }

    public @NotNull BaseComponent[] getPunishmentMessageBanned(@Nullable String reporterName,
                                                               @Nullable ZonedDateTime expirationDate,
                                                               @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(get("language.punishment.banned"), reporterName, reason, expirationDateString);
    }

    public @NotNull BaseComponent[] getPunishmentMessageMuted(@Nullable String reporterName,
                                                              @Nullable ZonedDateTime expirationDate,
                                                              @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(get("language.punishment.muted"), reporterName, reason, expirationDateString);
    }

    public @NotNull BaseComponent[] getPunishmentMessageBlockedCommandMuted() {
        return TextUtil.toComponentBungee(get("language.punishment.commands_muted"));
    }

    public @NotNull BaseComponent[] getPunishmentMessageKicked(@Nullable String reporterName, @Nullable String reason) {
        return getPunishmentMessage(get("language.punishment.kicked"), reporterName, reason, null);
    }

    public @NotNull BaseComponent[] getPunishmentMessageLoggedOff() {
        return TextUtil.toComponentBungee(get("language.punishment.logged_off"));
    }
}
