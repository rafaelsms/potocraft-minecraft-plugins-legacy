package com.rafaelsms.potocraft.loginmanager;

import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    public Configuration(@NotNull Path dataDirectory) throws IOException {
        super(getConfigurationFile(dataDirectory));
        loadConfiguration();
    }

    protected static @NotNull File getConfigurationFile(Path dataDirectory) throws IOException {
        File directory = dataDirectory.toFile();
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Couldn't create data folder");
        }
        return new File(directory, "config.yml");
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
        defaults.put(Keys.COMMAND_MULTIPLE_PROFILES_FOUND, "&6Várias respostas: &e%list%");
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
        defaults.put(Keys.COMMAND_REGISTER_LOGIN_INSTEAD,
                     "&6Conta já cadastrada! Digite &e&l/login &6e sua senha para entrar.");
        defaults.put(Keys.COMMAND_REGISTER_CHANGE_PIN_INSTEAD,
                     "&6Conta já cadastrada! Digite &e&l/mudarsenha &6para alterar a senha.");
        defaults.put(Keys.COMMAND_LOGIN_HELP,
                     "&6Lembre-se da senha cadastrada e digite: &e&l/login 123456 &6trocando pela &esua &6senha de 6 números.");
        defaults.put(Keys.COMMAND_LOGIN_REGISTER_INSTEAD,
                     "&6Conta ainda não cadastrada! Digite &e&l/registrar &6para registrar");
        defaults.put(Keys.COMMAND_LOGIN_ALREADY_LOGGED_IN, "&6Já está online, bom jogo :)");
        defaults.put(Keys.COMMAND_BAN_HELP, "&6Uso: &e&l/ban <regex> <razão>");
        defaults.put(Keys.COMMAND_BAN_PLAYER_OFFLINE, "&cJogador está offline, você não possui permissão.");
        defaults.put(Keys.COMMAND_TEMPORARY_BAN_HELP, "&6Uso: &e&l/tempban <regex> <tempo> <razão>");
        defaults.put(Keys.COMMAND_TEMPORARY_BAN_PLAYER_OFFLINE, "&cJogador está offline, você não possui permissão.");
        defaults.put(Keys.COMMAND_MUTE_HELP, "&6Uso: &e&l/mute <regex> <tempo> <razão>");
        defaults.put(Keys.COMMAND_MUTE_PLAYER_OFFLINE, "&cJogador está offline, você não possui permissão.");
        defaults.put(Keys.COMMAND_KICK_HELP, "&6Uso: &e&l/kick <regex> <razão>");
        defaults.put(Keys.COMMAND_HISTORY_HELP, "&6Uso: &e&l/history <regex>");

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

    public @NotNull DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(get(Keys.DATE_TIME_FORMATTER));
    }

    public Component getNoPermission() {
        return TextUtil.toComponent(get(Keys.GENERIC_NO_PERMISSION));
    }

    public Component getNoPlayerFound() {
        return TextUtil.toComponent(get(Keys.GENERIC_NO_PLAYER_FOUND));
    }

    public Component getCommandPlayersOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_ONLY));
    }

    public Component getCommandOfflinePlayersOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_OFFLINE_PLAYER_ONLY));
    }

    public Component getCommandIncorrectPin() {
        return TextUtil.toComponent(get(Keys.COMMAND_INCORRECT_PIN));
    }

    public Component getCommandIncorrectPinFormat() {
        return TextUtil.toComponent(get(Keys.COMMAND_INCORRECT_PIN_FORMAT));
    }

    public Component getCommandLoggedInOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGGED_IN_ONLY));
    }

    public Component getCommandLoggedIn() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGGED_IN));
    }

    public Component getCommandNoProfileFound() {
        return TextUtil.toComponent(get(Keys.COMMAND_NO_PROFILE_FOUND));
    }

    public Component getCommandMultipleProfilesFound(@NotNull List<Profile> profiles) {
        return TextUtil
                .toComponent(get(Keys.COMMAND_MULTIPLE_PROFILES_FOUND))
                .replaceText(TextUtil.replaceText("%list%",
                                                  TextUtil.joinStrings(profiles, ", ", Profile::getLastPlayerName)));
    }

    public Component getCommandChangePinHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_HELP));
    }

    public Component getCommandChangePinInvalidPins() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_INVALID_PINS));
    }

    public Component getCommandChangePinRegisterFirst() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_REGISTER_INSTEAD));
    }

    public Component getCommandChangedPinSuccessful() {
        return TextUtil.toComponent(get(Keys.COMMAND_CHANGE_PIN_SUCCESSFUL));
    }

    public Component getCommandRegisterHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_HELP));
    }

    public Component getCommandRegisterInvalidPin() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_INVALID_PINS));
    }

    public Component getCommandRegisterShouldChangePinInstead() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_CHANGE_PIN_INSTEAD));
    }

    public Component getCommandRegisterShouldLoginInstead() {
        return TextUtil.toComponent(get(Keys.COMMAND_REGISTER_LOGIN_INSTEAD));
    }

    public Component getCommandLoginHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_HELP));
    }

    public Component getCommandLoginRegisterFirst() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_REGISTER_INSTEAD));
    }

    public Component getCommandLoginAlreadyLoggedIn() {
        return TextUtil.toComponent(get(Keys.COMMAND_LOGIN_ALREADY_LOGGED_IN));
    }

    public Component getCommandBanHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_BAN_HELP));
    }

    public Component getCommandBanPlayerOffline() {
        return TextUtil.toComponent(get(Keys.COMMAND_BAN_PLAYER_OFFLINE));
    }

    public Component getCommandTemporaryBanHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_TEMPORARY_BAN_HELP));
    }

    public Component getCommandTemporaryBanPlayerOffline() {
        return TextUtil.toComponent(get(Keys.COMMAND_TEMPORARY_BAN_PLAYER_OFFLINE));
    }

    public Component getCommandMuteHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_MUTE_HELP));
    }

    public Component getCommandMutePlayerOffline() {
        return TextUtil.toComponent(get(Keys.COMMAND_MUTE_PLAYER_OFFLINE));
    }

    public Component getCommandKickHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_KICK_HELP));
    }

    public Component getCommandHistoryHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_HISTORY_HELP));
    }

    public Component getKickMessageCouldNotCheckMojangUsername() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_COULD_NOT_CHECK_MOJANG));
    }

    public Component getKickMessageInvalidPrefixForJavaPlayer() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_INVALID_PREFIX_JAVA_PLAYER));
    }

    public Component getKickMessageInvalidUsername() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_INVALID_USERNAME));
    }

    public Component getKickMessageFailedToRetrieveProfile() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_FAILED_TO_RETRIEVE_PROFILE));
    }

    public Component getKickMessageFailedToSaveProfile() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_FAILED_TO_SAVE_PROFILE));
    }

    public Component getKickMessageLoginServerUnavailable() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_LOGIN_SERVER_UNAVAILABLE));
    }

    private Component getPunishmentMessage(@NotNull String baseMessage,
                                           @Nullable String reporterName,
                                           @Nullable String reason,
                                           @Nullable String expirationDate) {
        String reporterFallback = get(Keys.GENERIC_UNKNOWN_PLAYER);
        String reasonFallback = get(Keys.GENERIC_UNKNOWN_REPORT_REASON);
        String expirationDateFallback = get(Keys.GENERIC_NO_EXPIRATION_DATE);
        return TextUtil
                .toComponent(baseMessage)
                .replaceText(TextUtil.replaceText("%reporter%", Util.getOrElse(reporterName, reporterFallback)))
                .replaceText(TextUtil.replaceText("%reason%", Util.getOrElse(reason, reasonFallback)))
                .replaceText(TextUtil.replaceText("%expiration_date%",
                                                  Util.getOrElse(expirationDate, expirationDateFallback)));
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
        return TextUtil.toComponent(get(Keys.PUNISHMENT_COMMANDS_MUTED));
    }

    public Component getPunishmentMessageKicked(@Nullable String reporterName, @Nullable String reason) {
        return getPunishmentMessage(get(Keys.PUNISHMENT_KICKED), reporterName, reason, null);
    }

    public Component getPunishmentMessageLoggedOff() {
        return TextUtil.toComponent(get(Keys.PUNISHMENT_LOGGED_OFF));
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
        public static final String OFFLINE_PLAYERS_ALLOWED_COMMANDS = "configuration.offline_allowed_commands";
        public static final String MUTED_BLOCKED_COMMANDS = "configuration.muted_blocked_commands";
        public static final String ALLOWED_USERNAMES_REGEX = "configuration.allowed_usernames_regex";


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
        public static final String COMMAND_MULTIPLE_PROFILES_FOUND = "language.commands.multiple_profiles_found";
        public static final String COMMAND_CHANGE_PIN_HELP = "language.commands.change_pin.help";
        public static final String COMMAND_CHANGE_PIN_INVALID_PINS = "language.commands.change_pin.invalid_pins";
        public static final String COMMAND_CHANGE_PIN_REGISTER_INSTEAD =
                "language.commands.change_pin.register_instead";
        public static final String COMMAND_CHANGE_PIN_SUCCESSFUL = "language.commands.change_pin.success";
        public static final String COMMAND_REGISTER_HELP = "language.commands.register.help";
        public static final String COMMAND_REGISTER_INVALID_PINS = "language.commands.register.invalid_pins";
        public static final String COMMAND_REGISTER_LOGIN_INSTEAD = "language.commands.register.login_instead";
        public static final String COMMAND_REGISTER_CHANGE_PIN_INSTEAD =
                "language.commands.register.change_pin_instead";
        public static final String COMMAND_LOGIN_HELP = "language.commands.login.help";
        public static final String COMMAND_LOGIN_REGISTER_INSTEAD = "language.commands.login.register_instead";
        public static final String COMMAND_LOGIN_ALREADY_LOGGED_IN = "language.commands.login.already_logged_in";
        public static final String COMMAND_BAN_HELP = "language.commands.ban.help";
        public static final String COMMAND_TEMPORARY_BAN_HELP = "language.commands.temporary_ban.help";
        public static final String COMMAND_TEMPORARY_BAN_PLAYER_OFFLINE =
                "language.commands.temporary_ban.player_offline";
        public static final String COMMAND_BAN_PLAYER_OFFLINE = "language.commands.ban.player_offline";
        public static final String COMMAND_MUTE_HELP = "language.commands.mute.help";
        public static final String COMMAND_MUTE_PLAYER_OFFLINE = "language.commands.mute.player_offline";
        public static final String COMMAND_KICK_HELP = "language.commands.kick.help";
        public static final String COMMAND_HISTORY_HELP = "language.commands.history.help";

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
