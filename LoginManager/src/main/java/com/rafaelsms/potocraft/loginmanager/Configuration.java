package com.rafaelsms.potocraft.loginmanager;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    public Configuration(@NotNull Path dataDirectory) throws IOException {
        super(dataDirectory.toFile(), "config.yml");
        loadConfiguration();
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        /* Configuration */
        defaults.put(Keys.MONGO_URI, "mongodb://localhost:27017");
        defaults.put(Keys.MONGO_DATABASE, "proxyDb");
        defaults.put(Keys.MONGO_PLAYER_PROFILE_COLLECTION, "loginProfiles");
        defaults.put(Keys.MONGO_DATABASE_ERROR_FATAL, false);

        defaults.put(Keys.OFFLINE_PLAYERS_LOGIN_SERVER, "login");
        defaults.put(Keys.OFFLINE_PLAYERS_AUTO_LOGIN_MINUTES, 45);
        defaults.put(Keys.OFFLINE_MAX_ACCOUNTS_PER_ADDRESS, 1);
        defaults.put(Keys.TAB_LIST_OTHER_SERVER_FORMAT, "&7(%server_name%) %prefix%%player_name%%suffix%");
        defaults.put(Keys.OFFLINE_PLAYERS_ALLOWED_COMMANDS,
                     List.of("login", "l", "log", "registrar", "cadastrar", "reg", "register"));
        defaults.put(Keys.MUTED_BLOCKED_COMMANDS,
                     List.of("msg",
                             "tell",
                             "pm",
                             "dm",
                             "reply",
                             "r",
                             "w",
                             "whisper",
                             "responder",
                             "message",
                             "mensagem",
                             "tp",
                             "tpa",
                             "tpyes",
                             "tpsim",
                             "tpask",
                             "tpaqui",
                             "tphere",
                             "tpahere",
                             "tpaccept",
                             "tpaceitar",
                             "teleporteaqui",
                             "teleporthere",
                             "teleporte",
                             "teleport",
                             "teleportaccept",
                             "teleportaceitar"));
        defaults.put(Keys.ALLOWED_USERNAMES_REGEX, "^*?[A-Za-z0-9_]{3,16}$");
        defaults.put(Keys.DATE_TIME_FORMATTER, "EEE', 'dd' de 'MMM' às 'H'h'mm");


        /* Language */
        defaults.put(Keys.GENERIC_CONSOLE_NAME, "Console (administração)");
        defaults.put(Keys.GENERIC_UNKNOWN_PLAYER, "(desconhecido)");
        defaults.put(Keys.GENERIC_UNKNOWN_REPORT_REASON, "não especificado");
        defaults.put(Keys.GENERIC_NO_EXPIRATION_DATE, "data não especificada");
        defaults.put(Keys.GENERIC_NO_PERMISSION, "&cVocê não possui permissão.");
        defaults.put(Keys.GENERIC_NO_PLAYER_FOUND, "&cNenhum jogador encontrado.");

        defaults.put(Keys.COMMAND_PLAYER_ONLY, "&cComando pode ser executado apenas por jogadores!");
        defaults.put(Keys.COMMAND_OFFLINE_PLAYER_ONLY, "&cComando pode ser executado apenas por jogadores piratas!");
        defaults.put(Keys.COMMAND_LOGGED_IN_ONLY,
                     "&cComando pode ser executado apenas por jogadores que usaram &e&l/login&c!");
        defaults.put(Keys.COMMAND_INCORRECT_PIN, "&cSenha inválida!");
        defaults.put(Keys.COMMAND_LOGGED_IN, "&aLogin com sucesso!");
        defaults.put(Keys.COMMAND_INCORRECT_PIN_FORMAT, "&cSua senha deve ter 6 números.");
        defaults.put(Keys.COMMAND_NO_PROFILE_FOUND, "&6Não encontramos usuários com este nome.");
        defaults.put(Keys.COMMAND_FAILED_TO_SEARCH_PROFILE, "&cFalha ao procurar perfil.");
        defaults.put(Keys.COMMAND_FAILED_TO_SAVE_PROFILE, "&cFalha ao salvar perfil.");
        defaults.put(Keys.COMMAND_CHANGE_PIN_HELP, """
                                                   &6Lembre da sua senha anterior, pense numa senha nova de 6 números e digite:
                                                   &e&l/mudarsenha <antiga> <nova> <nova>
                                                   &6Por exemplo: &e/mudarsenha 000000 123456 123456
                                                   """);
        defaults.put(Keys.COMMAND_CHANGE_PIN_INVALID_PINS, """
                                                           &cSua senha nova precisa ser digitada igualmente duas vezes.
                                                           &cA senha precisa ser 6 números.
                                                           &6Exemplo: &a&l/mudarsenha <antiga> 123456 123456
                                                           """);
        defaults.put(Keys.COMMAND_CHANGE_PIN_REGISTER_INSTEAD, """
                                                               &6Você ainda não tem senha cadastrada, digite para cadastrar:
                                                               &a&l/registrar
                                                                """);
        defaults.put(Keys.COMMAND_CHANGE_PIN_SUCCESSFUL, "&6Senha alterada com sucesso!");
        defaults.put(Keys.COMMAND_REGISTER_HELP, """
                                                 &6Pense e memorize uma senha de 6 números e digite duas vezes:
                                                 &6Por exemplo: &e&l/registrar 123456 123456
                                                 """);
        defaults.put(Keys.COMMAND_REGISTER_INVALID_PINS, """
                                                         &6Sua senha deve conter 6 números e ser digitada duas vezes de forma igual:
                                                         &6Por exemplo: &e&l/registrar 012345 012345
                                                         """);
        defaults.put(Keys.COMMAND_REGISTER_MAX_ACCOUNT_PER_ADDRESS,
                     "&cNúmero máximo de contas para seu endereço atingido.");
        defaults.put(Keys.COMMAND_REGISTER_LOGIN_INSTEAD,
                     "&6Conta já cadastrada! Digite &e&l/login &6e sua senha para entrar.");
        defaults.put(Keys.COMMAND_REGISTER_CHANGE_PIN_INSTEAD,
                     "&6Conta já cadastrada! Digite &e&l/mudarsenha &6para alterar a senha.");
        defaults.put(Keys.COMMAND_LOGIN_HELP,
                     "&6Lembre-se da senha cadastrada e digite: &e&l/login 123456 &6trocando pela &esua &6senha de 6 números.");
        defaults.put(Keys.COMMAND_LOGIN_REGISTER_INSTEAD,
                     "&6Conta ainda não cadastrada! Digite &e&l/registrar &6para registrar");
        defaults.put(Keys.COMMAND_LOGIN_ALREADY_LOGGED_IN, "&6Já está online, bom jogo :)");
        defaults.put(Keys.COMMAND_LOGIN_NO_SERVER_AVAILABLE, """
                                                             &cFalha ao transferir: servidor indisponível.
                                                             &cDigite &e&l/server &cpara mudar de servidor manualmente.
                                                             """);
        defaults.put(Keys.COMMAND_UNBAN_HELP, "&6Uso: &e&l/unban <nome>");
        defaults.put(Keys.COMMAND_BAN_HELP, "&6Uso: &e&l/ban <nome> <razão>");
        defaults.put(Keys.COMMAND_BAN_PLAYER_OFFLINE, "&cJogador está offline, você não possui permissão.");
        defaults.put(Keys.COMMAND_TEMPORARY_BAN_HELP, "&6Uso: &e&l/tempban <nome> <tempo> <razão>");
        defaults.put(Keys.COMMAND_TEMPORARY_BAN_PLAYER_OFFLINE, "&cJogador está offline, você não possui permissão.");
        defaults.put(Keys.COMMAND_UNMUTE_HELP, "&6Uso: &e&l/unmute <nome>");
        defaults.put(Keys.COMMAND_MUTE_HELP, "&6Uso: &e&l/mute <nome> <tempo> <razão>");
        defaults.put(Keys.COMMAND_MUTE_PLAYER_OFFLINE, "&cJogador está offline, você não possui permissão.");
        defaults.put(Keys.COMMAND_KICK_HELP, "&6Uso: &e&l/kick <nome> <razão>");
        defaults.put(Keys.COMMAND_PLAYER_PUNISHED, "&6Jogador &e%player% &6punido.");
        defaults.put(Keys.COMMAND_PLAYER_UNPUNISHED, "&6%player% teve sua punição revogada.");
        defaults.put(Keys.COMMAND_PLAYER_IS_NOT_PUNISHED, "&c%player% não está punido.");
        defaults.put(Keys.COMMAND_LIST_SERVER_LIST, "&6Servidor %server_name% (%size%): &e%player_list%");
        defaults.put(Keys.COMMAND_SEEN_HELP, "&6Uso: &e&l/seen <nome/uuid>");
        defaults.put(Keys.COMMAND_SEEN_REPORT_ENTRY,
                     "&e%type% &6%active% de &e%reporter_id% &6até &e%expiration_date% &6por &e%reason%");
        defaults.put(Keys.COMMAND_SEEN_PROFILE, """
                                                &e&l%user_name% &6(%user_id%):
                                                &6* Último servidor: &e%server_name%
                                                &6* Tempo em jogo: &e%play_time%
                                                &6* Última vez online: &e%join_date%
                                                &6* Última saída: &e%quit_date%
                                                &6* Ocorrências:
                                                %report_entries%
                                                """);

        defaults.put(Keys.KICK_MESSAGE_COULD_NOT_CHECK_MOJANG, """
                                                               &cFalha ao consultar servidor da Microsoft.
                                                               &cTente novamente mais tarde!
                                                               """);
        defaults.put(Keys.KICK_MESSAGE_INVALID_PREFIX_JAVA_PLAYER, """
                                                                   &cNome inválido para Java Edition.
                                                                   &cTroque de nome e entre novamente.
                                                                   """);
        defaults.put(Keys.KICK_MESSAGE_INVALID_USERNAME, """
                                                         &cNome inválido.
                                                         &cTroque de nome e entre novamente.
                                                         """);
        defaults.put(Keys.KICK_MESSAGE_FAILED_TO_RETRIEVE_PROFILE, """
                                                                   &cFalha ao consultar seu perfil.
                                                                   &cTente novamente mais tarde!
                                                                   """);
        defaults.put(Keys.KICK_MESSAGE_FAILED_TO_SAVE_PROFILE, """
                                                               &cFalha ao salvar seu perfil.
                                                               &cTente novamente mais tarde!
                                                               """);
        defaults.put(Keys.KICK_MESSAGE_LOGIN_SERVER_UNAVAILABLE, """
                                                                 &cServidor para login indisponível.
                                                                 &cTente novamente mais tarde!
                                                                 """);
        defaults.put(Keys.KICK_MESSAGE_GENERIC, "&cVocê foi expulso do servidor");

        defaults.put(Keys.PUNISHMENT_MUTED,
                     "&cVocê foi silenciado por &e%reporter% &cpelo motivo &e%reason% &caté &e%expiration_date%");
        defaults.put(Keys.PUNISHMENT_BANNED, """
                                             &cVocê foi banido por &e%reporter%
                                             &cpelo motivo &e%reason%
                                             &caté &e%expiration_date%
                                             """);
        defaults.put(Keys.PUNISHMENT_KICKED, """
                                             &cVocê foi expulso por &e%reporter%
                                             &cpor &e%reason%
                                             """);
        defaults.put(Keys.PUNISHMENT_LOGGED_OFF, "&6Você não efetuou login! Digite &e&l/registrar &6ou &e&l/login&6!");
        return defaults;
    }

    public String getMongoURI() {
        return get(Keys.MONGO_URI);
    }

    public String getMongoDatabase() {
        return get(Keys.MONGO_DATABASE);
    }

    public String getMongoPlayerProfileCollection() {
        return get(Keys.MONGO_PLAYER_PROFILE_COLLECTION);
    }

    public boolean isMongoDatabaseFailureFatal() {
        return get(Keys.MONGO_DATABASE_ERROR_FATAL);
    }

    public List<String> getAllowedCommandsLoggedOff() {
        return get(Keys.OFFLINE_PLAYERS_ALLOWED_COMMANDS);
    }

    public List<String> getBlockedCommandsMuted() {
        return get(Keys.MUTED_BLOCKED_COMMANDS);
    }

    public Pattern getAllowedUsernamesRegex() {
        return Pattern.compile(get(Keys.ALLOWED_USERNAMES_REGEX), Pattern.CASE_INSENSITIVE);
    }

    public String getLoginServer() {
        return get(Keys.OFFLINE_PLAYERS_LOGIN_SERVER);
    }

    public Duration getAutoLoginWindow() {
        Integer minutes = get(Keys.OFFLINE_PLAYERS_AUTO_LOGIN_MINUTES);
        if (minutes.longValue() < 0) {
            return Duration.ZERO;
        }
        return Duration.ofMinutes(minutes.longValue());
    }

    public int getMaxAccountsPerAddress() {
        return get(Keys.OFFLINE_MAX_ACCOUNTS_PER_ADDRESS);
    }

    public Component getTabDisplayName(@NotNull Player player, @NotNull String playerServer) {
        return TextUtil.toComponent(get(Keys.TAB_LIST_OTHER_SERVER_FORMAT))
                       .replace("%server_name%", playerServer)
                       .replace("%prefix%", TextUtil.getPrefix(player.getUniqueId()))
                       .replace("%player_name%", player.getUsername())
                       .replace("%suffix%", TextUtil.getSuffix(player.getUniqueId()))
                       .build();
    }

    public @NotNull DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(get(Keys.DATE_TIME_FORMATTER));
    }

    public Component getNoPermission() {
        return TextUtil.toComponent(get(Keys.GENERIC_NO_PERMISSION)).build();
    }

    public Component getNoPlayerFound() {
        return TextUtil.toComponent(get(Keys.GENERIC_NO_PLAYER_FOUND)).build();
    }

    public Component getCommandPlayersOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_ONLY)).build();
    }

    public Component getCommandOfflinePlayersOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_OFFLINE_PLAYER_ONLY)).build();
    }

    public Component getCommandIncorrectPin() {
        return TextUtil.toComponent(get(Keys.COMMAND_INCORRECT_PIN)).build();
    }

    public Component getCommandIncorrectPinFormat() {
        return TextUtil.toComponent(get(Keys.COMMAND_INCORRECT_PIN_FORMAT)).build();
    }

    public Component getCommandLoggedInOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGGED_IN_ONLY)).build();
    }

    public Component getCommandLoggedIn() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGGED_IN)).build();
    }

    public Component getCommandFailedToSearchProfile() {
        return TextUtil.toComponent(get(Keys.COMMAND_FAILED_TO_SEARCH_PROFILE)).build();
    }

    public Component getCommandNoProfileFound() {
        return TextUtil.toComponent(get(Keys.COMMAND_NO_PROFILE_FOUND)).build();
    }

    public Component getCommandFailedToSaveProfile() {
        return TextUtil.toComponent(get(Keys.COMMAND_FAILED_TO_SAVE_PROFILE)).build();
    }

    public Component getCommandChangePinHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_HELP)).build();
    }

    public Component getCommandChangePinInvalidPins() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_INVALID_PINS)).build();
    }

    public Component getCommandChangePinRegisterFirst() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_REGISTER_INSTEAD)).build();
    }

    public Component getCommandChangedPinSuccessful() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_SUCCESSFUL)).build();
    }

    public Component getCommandRegisterHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_HELP)).build();
    }

    public Component getCommandRegisterInvalidPin() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_INVALID_PINS)).build();
    }

    public Component getCommandRegisterAccountLimitForAddress() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_MAX_ACCOUNT_PER_ADDRESS)).build();
    }

    public Component getCommandRegisterShouldChangePinInstead() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_CHANGE_PIN_INSTEAD)).build();
    }

    public Component getCommandRegisterShouldLoginInstead() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_LOGIN_INSTEAD)).build();
    }

    public Component getCommandLoginHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_HELP)).build();
    }

    public Component getCommandLoginRegisterFirst() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_REGISTER_INSTEAD)).build();
    }

    public Component getCommandLoginAlreadyLoggedIn() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_ALREADY_LOGGED_IN)).build();
    }

    public Component getCommandLoginNoServerAvailable() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_NO_SERVER_AVAILABLE)).build();
    }

    public Component getCommandUnbanHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_UNBAN_HELP)).build();
    }

    public Component getCommandUnpunished(@NotNull String playerName) {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_UNPUNISHED)).replace("%player%", playerName).build();
    }

    public Component getCommandPlayerIsNotPunished(@NotNull String playerName) {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_IS_NOT_PUNISHED)).replace("%player%", playerName).build();
    }

    public Component getCommandBanHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_BAN_HELP)).build();
    }

    public Component getCommandBanPlayerOffline() {
        return TextUtil.toComponent(get(Keys.COMMAND_BAN_PLAYER_OFFLINE)).build();
    }

    public Component getPlayerPunished(@NotNull String playerName) {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_PUNISHED)).replace("%player%", playerName).build();
    }

    public Component getListServerList(@NotNull String serverName, @NotNull Collection<Player> playerList) {
        return TextUtil.toComponent(get(Keys.COMMAND_LIST_SERVER_LIST))
                       .replace("%server_name%", serverName)
                       .replace("%size%", String.valueOf(playerList.size()))
                       .replace("%player_list%", TextUtil.joinStrings(playerList, ", ", Player::getUsername))
                       .build();
    }

    public Component getCommandSeenHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_SEEN_HELP)).build();
    }

    public Component getCommandSeenReportEntries(@NotNull Collection<ReportEntry> reportEntries) {
        List<Component> lines = new ArrayList<>(reportEntries.size());
        for (ReportEntry entry : reportEntries) {
            lines.add(TextUtil.toComponent(get(Keys.COMMAND_SEEN_REPORT_ENTRY))
                              .replace("%reporter_id%",
                                       Util.convertFallback(entry.getReporterId(), UUID::toString, "console"))
                              .replace("%type%", entry.getType())
                              .replace("%active%", entry.isActive() ? "enabled" : "disabled")
                              .replace("%expiration_date%",
                                       Util.convertFallback(entry.getExpirationDate(),
                                                            getDateTimeFormatter()::format,
                                                            "?"))
                              .replace("%reason%", Util.getOrElse(entry.getReason(), "?"))
                              .build());
        }
        return Component.join(Component.newline(), lines);
    }

    public Component getCommandSeen(@NotNull Profile profile) {
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
        return TextUtil.toComponent(get(Keys.COMMAND_SEEN_PROFILE))
                       .replace("%user_name%", profile.getLastPlayerName())
                       .replace("%user_id%", profile.getPlayerId().toString())
                       .replace("%server_name%", profile.getLastServerName().orElse("?"))
                       .replace("%play_time%", playTime)
                       .replace("%join_date%", lastJoinDate)
                       .replace("%quit_date%", lastQuitDate)
                       .replace("%report_entries%", getCommandSeenReportEntries(profile.getReportEntries()))
                       .build();
    }

    public Component getCommandTemporaryBanHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_TEMPORARY_BAN_HELP)).build();
    }

    public Component getCommandTemporaryBanPlayerOffline() {
        return TextUtil.toComponent(get(Keys.COMMAND_TEMPORARY_BAN_PLAYER_OFFLINE)).build();
    }

    public Component getCommandUnmuteHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_UNMUTE_HELP)).build();
    }

    public Component getCommandMuteHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_MUTE_HELP)).build();
    }

    public Component getCommandMutePlayerOffline() {
        return TextUtil.toComponent(get(Keys.COMMAND_MUTE_PLAYER_OFFLINE)).build();
    }

    public Component getCommandKickHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_KICK_HELP)).build();
    }

    public Component getKickMessageCouldNotCheckMojangUsername() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_COULD_NOT_CHECK_MOJANG)).build();
    }

    public Component getKickMessageInvalidPrefixForJavaPlayer() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_INVALID_PREFIX_JAVA_PLAYER)).build();
    }

    public Component getKickMessageInvalidUsername() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_INVALID_USERNAME)).build();
    }

    public Component getKickMessageFailedToRetrieveProfile() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_FAILED_TO_RETRIEVE_PROFILE)).build();
    }

    public Component getKickMessageFailedToSaveProfile() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_FAILED_TO_SAVE_PROFILE)).build();
    }

    public Component getKickMessageLoginServerUnavailable() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_LOGIN_SERVER_UNAVAILABLE)).build();
    }

    public Component getKickMessageGeneric() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_GENERIC)).build();
    }

    private Component getPunishmentMessage(@NotNull String baseMessage,
                                           @Nullable String reporterName,
                                           @Nullable String reason,
                                           @Nullable String expirationDate) {
        String reporterFallback = get(Keys.GENERIC_UNKNOWN_PLAYER);
        String reasonFallback = get(Keys.GENERIC_UNKNOWN_REPORT_REASON);
        String expirationDateFallback = get(Keys.GENERIC_NO_EXPIRATION_DATE);
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
        return getPunishmentMessage(get(Keys.PUNISHMENT_BANNED), reporterName, reason, expirationDateString);
    }

    public Component getPunishmentMessageMuted(@Nullable String reporterName,
                                               @Nullable ZonedDateTime expirationDate,
                                               @Nullable String reason) {
        String expirationDateString = Util.convert(expirationDate, date -> getDateTimeFormatter().format(date));
        return getPunishmentMessage(get(Keys.PUNISHMENT_MUTED), reporterName, reason, expirationDateString);
    }

    public Component getPunishmentMessageBlockedCommandMuted() {
        return TextUtil.toComponent(get(Keys.PUNISHMENT_COMMANDS_MUTED)).build();
    }

    public Component getPunishmentMessageKicked(@Nullable String reporterName, @Nullable String reason) {
        return getPunishmentMessage(get(Keys.PUNISHMENT_KICKED), reporterName, reason, null);
    }

    public Component getPunishmentMessageLoggedOff() {
        return TextUtil.toComponent(get(Keys.PUNISHMENT_LOGGED_OFF)).build();
    }

    private static final class Keys {

        /* Configuration */
        public static final String MONGO_URI = "configuration.database.mongo_uri";
        public static final String MONGO_DATABASE = "configuration.database.mongo_database";
        public static final String MONGO_PLAYER_PROFILE_COLLECTION = "configuration.database.player_profile_collection";
        public static final String MONGO_DATABASE_ERROR_FATAL = "configuration.database.database_failure_fatal";

        public static final String OFFLINE_PLAYERS_LOGIN_SERVER = "configuration.offline_players_login_server";
        public static final String OFFLINE_PLAYERS_AUTO_LOGIN_MINUTES =
                "configuration.minutes_between_joins_to_auto_login";
        public static final String OFFLINE_MAX_ACCOUNTS_PER_ADDRESS =
                "configuration.max_accounts_registered_per_address";
        public static final String OFFLINE_PLAYERS_ALLOWED_COMMANDS = "configuration.offline_allowed_commands";
        public static final String MUTED_BLOCKED_COMMANDS = "configuration.muted_blocked_commands";
        public static final String ALLOWED_USERNAMES_REGEX = "configuration.allowed_usernames_regex";
        public static final String TAB_LIST_OTHER_SERVER_FORMAT = "configuration.tab_list_other_server_entry_format";


        /* Language */
        public static final String DATE_TIME_FORMATTER = "language.generic.date_time_formatter";
        public static final String GENERIC_CONSOLE_NAME = "language.generic.console_name";
        public static final String GENERIC_UNKNOWN_PLAYER = "language.generic.unknown_player";
        public static final String GENERIC_UNKNOWN_REPORT_REASON = "language.generic.report_reason_unknown";
        public static final String GENERIC_NO_EXPIRATION_DATE = "language.generic.no_expiration_date";
        public static final String GENERIC_NO_PERMISSION = "language.generic.no_permission";
        public static final String GENERIC_NO_PLAYER_FOUND = "language.generic.no_player_found";

        public static final String COMMAND_PLAYER_ONLY = "language.commands.players_only";
        public static final String COMMAND_OFFLINE_PLAYER_ONLY = "language.commands.offline_players_only";
        public static final String COMMAND_LOGGED_IN_ONLY = "language.commands.logged_in_players_only";
        public static final String COMMAND_INCORRECT_PIN = "language.commands.incorrect_pin";
        public static final String COMMAND_INCORRECT_PIN_FORMAT = "language.commands.incorrect_pin_format";
        public static final String COMMAND_LOGGED_IN = "language.commands.logged_in";
        public static final String COMMAND_NO_PROFILE_FOUND = "language.commands.no_profile_found";
        public static final String COMMAND_FAILED_TO_SEARCH_PROFILE = "language.commands.failed_to_search_profile";
        public static final String COMMAND_FAILED_TO_SAVE_PROFILE = "language.commands.failed_to_save_profile";
        public static final String COMMAND_CHANGE_PIN_HELP = "language.commands.change_pin.help";
        public static final String COMMAND_CHANGE_PIN_INVALID_PINS = "language.commands.change_pin.invalid_pins";
        public static final String COMMAND_CHANGE_PIN_REGISTER_INSTEAD =
                "language.commands.change_pin.register_instead";
        public static final String COMMAND_CHANGE_PIN_SUCCESSFUL = "language.commands.change_pin.success";
        public static final String COMMAND_REGISTER_HELP = "language.commands.register.help";
        public static final String COMMAND_REGISTER_INVALID_PINS = "language.commands.register.invalid_pins";
        public static final String COMMAND_REGISTER_MAX_ACCOUNT_PER_ADDRESS =
                "language.commands.register.max_accounts_per_address_reached";
        public static final String COMMAND_REGISTER_LOGIN_INSTEAD = "language.commands.register.login_instead";
        public static final String COMMAND_REGISTER_CHANGE_PIN_INSTEAD =
                "language.commands.register.change_pin_instead";
        public static final String COMMAND_LOGIN_HELP = "language.commands.login.help";
        public static final String COMMAND_LOGIN_REGISTER_INSTEAD = "language.commands.login.register_instead";
        public static final String COMMAND_LOGIN_ALREADY_LOGGED_IN = "language.commands.login.already_logged_in";
        public static final String COMMAND_LOGIN_NO_SERVER_AVAILABLE = "language.commands.login.no_server_available";
        public static final String COMMAND_UNBAN_HELP = "language.commands.unban.help";
        public static final String COMMAND_BAN_HELP = "language.commands.ban.help";
        public static final String COMMAND_TEMPORARY_BAN_HELP = "language.commands.temporary_ban.help";
        public static final String COMMAND_TEMPORARY_BAN_PLAYER_OFFLINE =
                "language.commands.temporary_ban.player_offline";
        public static final String COMMAND_BAN_PLAYER_OFFLINE = "language.commands.ban.player_offline";
        public static final String COMMAND_UNMUTE_HELP = "language.commands.unmute.help";
        public static final String COMMAND_MUTE_HELP = "language.commands.mute.help";
        public static final String COMMAND_MUTE_PLAYER_OFFLINE = "language.commands.mute.player_offline";
        public static final String COMMAND_KICK_HELP = "language.commands.kick.help";
        public static final String COMMAND_PLAYER_PUNISHED = "language.commands.player_punished";
        public static final String COMMAND_PLAYER_UNPUNISHED = "language.commands.player_unpunished";
        public static final String COMMAND_PLAYER_IS_NOT_PUNISHED = "language.commands.player_is_not_punished";
        public static final String COMMAND_LIST_SERVER_LIST = "language.commands.list_server_players";
        public static final String COMMAND_SEEN_HELP = "language.commands.seen.help";
        public static final String COMMAND_SEEN_REPORT_ENTRY = "language.commands.seen.report_entry";
        public static final String COMMAND_SEEN_PROFILE = "language.commands.seen.profile";

        public static final String KICK_MESSAGE_COULD_NOT_CHECK_MOJANG =
                "language.kick_messages.could_not_check_mojang";
        public static final String KICK_MESSAGE_INVALID_PREFIX_JAVA_PLAYER =
                "language.kick_messages.invalid_java_prefix";
        public static final String KICK_MESSAGE_INVALID_USERNAME = "language.kick_messages.invalid_username";
        public static final String KICK_MESSAGE_FAILED_TO_RETRIEVE_PROFILE =
                "language.kick_messages.failed_to_retrieve_player_profile";
        public static final String KICK_MESSAGE_FAILED_TO_SAVE_PROFILE =
                "language.kick_messages.failed_to_save_player_profile";
        public static final String KICK_MESSAGE_LOGIN_SERVER_UNAVAILABLE =
                "language.kick_messages.login_server_unavailable";
        public static final String KICK_MESSAGE_GENERIC = "language.kick_messages.generic_message";

        public static final String PUNISHMENT_LOGGED_OFF = "language.punishment.logged_off";
        public static final String PUNISHMENT_MUTED = "language.punishment.muted";
        public static final String PUNISHMENT_COMMANDS_MUTED = "language.punishment.commands_muted";
        public static final String PUNISHMENT_BANNED = "language.punishment.banned";
        public static final String PUNISHMENT_KICKED = "language.punishment.kicked";

        // Private constructor
        private Keys() {
        }

    }
}
