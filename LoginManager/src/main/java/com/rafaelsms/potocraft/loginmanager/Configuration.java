package com.rafaelsms.potocraft.loginmanager;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
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
        return TextUtil.toComponent(get("language.generic.no_permission")).buildBungee();
    }

    public @NotNull BaseComponent[] getNoPlayerFound() {
        return TextUtil.toComponent(get("language.generic.no_player_found")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandPlayersOnly() {
        return TextUtil.toComponent(get("language.commands.players_only")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandOfflinePlayersOnly() {
        return TextUtil.toComponent(get("language.commands.offline_players_only")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandIncorrectPin() {
        return TextUtil.toComponent(get("language.commands.incorrect_pin")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandIncorrectPinFormat() {
        return TextUtil.toComponent(get("language.commands.incorrect_pin_format")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandLoggedInOnly() {
        return TextUtil.toComponent(get("language.commands.logged_in_players_only")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandLoggedIn() {
        return TextUtil.toComponent(get("language.commands.logged_in")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandFailedToSearchProfile() {
        return TextUtil.toComponent(get("language.commands.failed_to_search_profile")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandNoProfileFound() {
        return TextUtil.toComponent(get("language.commands.no_profile_found")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandFailedToSaveProfile() {
        return TextUtil.toComponent(get("language.commands.failed_to_save_profile")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandChangePinHelp() {
        return TextUtil.toComponent(get("language.commands.change_pin.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandChangePinInvalidPins() {
        return TextUtil.toComponent(get("language.commands.change_pin.invalid_pins")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandChangePinRegisterFirst() {
        return TextUtil.toComponent(get("language.commands.change_pin.register_instead")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandChangedPinSuccessful() {
        return TextUtil.toComponent(get("language.commands.change_pin.success")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandRegisterHelp() {
        return TextUtil.toComponent(get("language.commands.register.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandRegisterInvalidPin() {
        return TextUtil.toComponent(get("language.commands.register.invalid_pins")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandRegisterAccountLimitForAddress() {
        return TextUtil.toComponent(get("language.commands.register.max_accounts_per_address_reached")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandRegisterShouldChangePinInstead() {
        return TextUtil.toComponent(get("language.commands.register.change_pin_instead")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandRegisterShouldLoginInstead() {
        return TextUtil.toComponent(get("language.commands.register.login_instead")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandLoginHelp() {
        return TextUtil.toComponent(get("language.commands.login.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandLoginRegisterFirst() {
        return TextUtil.toComponent(get("language.commands.login.register_instead")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandLoginAlreadyLoggedIn() {
        return TextUtil.toComponent(get("language.commands.login.already_logged_in")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandNoServerAvailable() {
        return TextUtil.toComponent(get("language.commands.login.no_server_available")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandUnbanHelp() {
        return TextUtil.toComponent(get("language.commands.unban.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandUnpunished(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.commands.player_unpunished"))
                       .replace("%player%", playerName)
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getCommandPlayerIsNotPunished(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.commands.player_is_not_punished"))
                       .replace("%player%", playerName)
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getCommandBanHelp() {
        return TextUtil.toComponent(get("language.commands.ban.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandBanPlayerOffline() {
        return TextUtil.toComponent(get("language.commands.ban.player_offline")).buildBungee();
    }

    public @NotNull BaseComponent[] getPlayerPunished(@NotNull String playerName) {
        return TextUtil.toComponent(get("language.commands.player_punished"))
                       .replace("%player%", playerName)
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getListServerList(@NotNull String serverName,
                                                      @NotNull Collection<ProxiedPlayer> playerList) {
        return TextUtil.toComponent(get("language.commands.list_server_players"))
                       .replace("%server_name%", serverName)
                       .replace("%size%", String.valueOf(playerList.size()))
                       .replace("%player_list%", TextUtil.joinStrings(playerList, ", ", ProxiedPlayer::getName))
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getCommandSeenHelp() {
        return TextUtil.toComponent(get("language.commands.seen.help")).buildBungee();
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
                                       Util.convertFallback(entry.getExpirationDate(),
                                                            getDateTimeFormatter()::format,
                                                            "?"))
                              .replace("%reason%", Util.getOrElse(entry.getReason(), "?"))
                              .build());
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
        return TextUtil.toComponent(get("language.commands.seen.profile"))
                       .replace("%user_name%", profile.getLastPlayerName())
                       .replace("%user_id%", profile.getPlayerId().toString())
                       .replace("%server_name%", profile.getLastServerName().orElse("?"))
                       .replace("%play_time%", playTime)
                       .replace("%join_date%", lastJoinDate)
                       .replace("%quit_date%", lastQuitDate)
                       .replace("%report_entries%", getCommandSeenReportEntries(plugin, profile.getReportEntries()))
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getCommandTemporaryBanHelp() {
        return TextUtil.toComponent(get("language.commands.temporary_ban.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandTemporaryBanPlayerOffline() {
        return TextUtil.toComponent(get("language.commands.temporary_ban.player_offline")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandUnmuteHelp() {
        return TextUtil.toComponent(get("language.commands.unmute.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandMuteHelp() {
        return TextUtil.toComponent(get("language.commands.mute.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandMutePlayerOffline() {
        return TextUtil.toComponent(get("language.commands.mute.player_offline")).buildBungee();
    }

    public @NotNull BaseComponent[] getCommandKickHelp() {
        return TextUtil.toComponent(get("language.commands.kick.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getKickMessageCouldNotCheckMojangUsername() {
        return TextUtil.toComponent(get("language.kick_messages.could_not_check_mojang")).buildBungee();
    }

    public @NotNull BaseComponent[] getKickMessageInvalidPrefixForJavaPlayer() {
        return TextUtil.toComponent(get("language.kick_messages.invalid_java_prefix")).buildBungee();
    }

    public @NotNull BaseComponent[] getKickMessageInvalidJavaUsername() {
        return TextUtil.toComponent(get("language.kick_messages.invalid_java_username")).buildBungee();
    }

    public @NotNull BaseComponent[] getKickMessageFailedToRetrieveProfile() {
        return TextUtil.toComponent(get("language.kick_messages.failed_to_retrieve_player_profile")).buildBungee();
    }

    public @NotNull BaseComponent[] getKickMessageFailedToSaveProfile() {
        return TextUtil.toComponent(get("language.kick_messages.failed_to_save_player_profile")).buildBungee();
    }

    public @NotNull BaseComponent[] getKickMessageLoginServerUnavailable() {
        return TextUtil.toComponent(get("language.kick_messages.login_server_unavailable")).buildBungee();
    }

    private @NotNull BaseComponent[] getPunishmentMessage(@Nullable String baseMessage,
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
                       .buildBungee();
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
        return TextUtil.toComponent(get("language.punishment.commands_muted")).buildBungee();
    }

    public @NotNull BaseComponent[] getPunishmentMessageKicked(@Nullable String reporterName, @Nullable String reason) {
        return getPunishmentMessage(get("language.punishment.kicked"), reporterName, reason, null);
    }

    public @NotNull BaseComponent[] getPunishmentMessageLoggedOff() {
        return TextUtil.toComponent(get("language.punishment.logged_off")).buildBungee();
    }
}
