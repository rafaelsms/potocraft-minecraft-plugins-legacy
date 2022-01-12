package com.rafaelsms.potocraft.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rafaelsms.potocraft.common.profile.Profile;
import com.rafaelsms.potocraft.common.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class Settings {

    private final Gson jsonConfig = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    protected final Map<String, Object> configuration = Collections.synchronizedMap(new LinkedHashMap<>());

    protected final @NotNull Plugin<?, ?, ?> plugin;

    protected Settings(@NotNull Plugin<?, ?, ?> plugin) throws Exception {
        this.plugin = plugin;

        // Create configuration file
        File configFile = plugin.getCommonServer().getConfigurationFile();
        if (!configFile.exists()) {
            setDefaults();
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(configFile))) {
                outputStream.write(jsonConfig.toJson(this.configuration).getBytes(StandardCharsets.UTF_8));
            }
        }
        // Read configuration file
        reloadFile();
    }

    public boolean isDebugMessagesEnabled() {
        return get(Constants.DEBUG_MESSAGES_ENABLED);
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(get(Constants.DATE_TIME_FORMAT));
    }

    public String getMongoURI() {
        return get(Constants.DATABASE_MONGO_URI);
    }

    public String getMongoDatabase() {
        return get(Constants.DATABASE_MONGO_DATABASE);
    }

    public boolean isMongoFailFatal() {
        return get(Constants.DATABASE_FAIL_FATAL);
    }

    public boolean isMongoFailPrintable() {
        return get(Constants.DATABASE_FAIL_PRINT_STACK);
    }

    public String getLoginServer() {
        return get(Constants.LOGIN_SERVER_NAME);
    }

    public Component getConsoleName() {
        return getLang(Constants.LANG_CONSOLE_NAME);
    }

    public Component getNoPermission() {
        return getLang(Constants.LANG_NO_PERMISSION);
    }

    public Component getPlayerNotFound() {
        return getLang(Constants.LANG_PLAYER_NOT_FOUND);
    }

    public Component getManyPlayersFound(@NotNull List<? extends Profile> profiles) {
        List<String> playerNames = new ArrayList<>();
        for (Profile profile : profiles) {
            playerNames.add(profile.getLastPlayerName());
        }
        TextReplacementConfig playerList = TextUtil.replaceText("%list%", TextUtil.joinStrings(playerNames, ", "));
        return getLang(Constants.LANG_MANY_PLAYERS_FOUND).replaceText(playerList);
    }

    public Component getUnknownPlayerName() {
        return getLang(Constants.LANG_UNKNOWN_PLAYER_NAME);
    }

    public Component getCommandConsoleCantExecute() {
        return getLang(Constants.LANG_CONSOLE_CANT_EXECUTE_COMMAND);
    }

    public Component getCommandGenericError() {
        return getLang(Constants.LANG_GENERIC_COMMAND_ERROR);
    }

    public Component getKickMessageCouldNotRetrieveProfile() {
        return getLang(Constants.LANG_COULD_NOT_RETRIEVE_PROFILE);
    }

    public Component getKickMessageCouldNotSaveProfile() {
        return getLang(Constants.LANG_COULD_NOT_SAVE_PROFILE);
    }

    public Component getChatMessagesTooFrequent() {
        return getLang(Constants.LANG_CHAT_TOO_FREQUENT);
    }

    public Component getChatMessagesTooSimilar() {
        return getLang(Constants.LANG_CHAT_SIMILAR);
    }

    protected Component getLang(@NotNull String key) {
        try {
            return TextUtil.getLang(get(key));
        } catch (Exception exception) {
            plugin
                    .logger()
                    .warn("Couldn't get language for \"%s\": %s".formatted(key, exception.getLocalizedMessage()));
            exception.printStackTrace();
            return Component.text(key);
        }
    }

    protected <T> void setDefault(@NotNull String key, T tDefault) {
        configuration.putIfAbsent(key, tDefault);
    }

    @SuppressWarnings("unchecked")
    protected <T> T get(@NotNull String key) {
        return (T) configuration.get(key);
    }

    public void reloadFile() throws Exception {
        File configFile = plugin.getCommonServer().getConfigurationFile();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(configFile))) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked") Map<String, ?> map = (Map<String, ?>) jsonConfig.fromJson(json, Map.class);
            try {
                new HashMap<>(this.configuration).putAll(map);
            } catch (Exception exception) {
                plugin.logger().warn("Failed to update settings: %s".formatted(exception.getLocalizedMessage()));
                return;
            }
            this.configuration.putAll(map);
        }
    }

    protected void setDefaults() {
        setDefault(Constants.DEBUG_MESSAGES_ENABLED, false);
        setDefault(Constants.DATE_TIME_FORMAT, "EEE', 'dd' de 'MMM' às 'H'h'mm"); // Sex, 13 de Dez às 15h54

        setDefault(Constants.DATABASE_MONGO_URI, "mongodb://localhost:27017");
        setDefault(Constants.DATABASE_MONGO_DATABASE, "potocraftDb");
        setDefault(Constants.DATABASE_FAIL_FATAL, true);
        setDefault(Constants.DATABASE_FAIL_PRINT_STACK, true);

        setDefault(Constants.LOGIN_SERVER_NAME, "login");

        setDefault(Constants.LANG_UNKNOWN_PLAYER_NAME, "&7(&cdesconhecido&7)");
        setDefault(Constants.LANG_CONSOLE_NAME, "&cConsole");

        setDefault(Constants.LANG_NO_PERMISSION, "&cVocê não tem permissão para executar isto.");
        setDefault(Constants.LANG_PLAYER_NOT_FOUND, "&cPessoa não encontrada.");
        setDefault(Constants.LANG_MANY_PLAYERS_FOUND, "&cVárias pessoas encontradas: &e%list%");
        setDefault(Constants.LANG_CONSOLE_CANT_EXECUTE_COMMAND, "&cConsole não pode executar este comando.");
        setDefault(Constants.LANG_GENERIC_COMMAND_ERROR, "&cFalha ao executar comando.");
        setDefault(Constants.LANG_COULD_NOT_RETRIEVE_PROFILE, "&cFalha ao acessar perfil.");
        setDefault(Constants.LANG_COULD_NOT_SAVE_PROFILE, "&cFalha ao salvar perfil.");

        setDefault(Constants.LANG_CHAT_TOO_FREQUENT, "&cMensagens muito rápidas.");
        setDefault(Constants.LANG_CHAT_SIMILAR, "&cMensagens muito parecidas.");
    }

    protected static final class Constants {

        /* Settings */
        // Common
        public static final String DEBUG_MESSAGES_ENABLED = "configuration.debug_messages_enabled";
        public static final String DATE_TIME_FORMAT = "configuration.date_time_format";

        public static final String DATABASE_MONGO_URI = "configuration.database.mongo_uri";
        public static final String DATABASE_MONGO_DATABASE = "configuration.database.mongo_database";
        public static final String DATABASE_FAIL_FATAL = "configuration.database.database_fail_shutdowns_server";
        public static final String DATABASE_FAIL_PRINT_STACK =
                "configuration.database.database_fail_prints_stack_trace";

        public static final String LOBBY_SERVER_NAME = "configuration.login.lobby_server";

        // Velocity
        public static final String LOGIN_MAX_LOGIN_DURATION_SECONDS =
                "configuration.login.max_time_since_last_login_to_auto_login_seconds";
        public static final String LOGIN_SERVER_NAME = "configuration.login.unauthenticated_server";
        public static final String KICK_IF_LOBBY_UNAVAILABLE = "configuration.login.kick_if_lobby_server_unavailable";

        public static final String UNIVERSAL_CHAT_FORMAT = "configuration.chat.universal.format";
        public static final String UNIVERSAL_CHAT_SPY_FORMAT = "configuration.chat.universal.format";
        public static final String UNIVERSAL_CHAT_PREFIX = "configuration.chat.universal.prefix";
        public static final String UNIVERSAL_CHAT_LIMITER_MESSAGES_AMOUNT =
                "configuration.chat.universal.limiter.messages_amount";
        public static final String UNIVERSAL_CHAT_LIMITER_TIME_AMOUNT =
                "configuration.chat.universal.limiter.time_amount_millis";
        public static final String UNIVERSAL_CHAT_COMPARATOR_MIN_LENGTH =
                "configuration.chat.universal.comparator.minimum_length";
        public static final String UNIVERSAL_CHAT_COMPARATOR_THRESHOLD =
                "configuration.chat.universal.comparator.minimum_char_differences";
        public static final String REPLY_MESSAGE_TIMEOUT =
                "configuration.chat.direct_messages.reply_candidate_timeout_in_seconds";
        public static final String DIRECT_MESSAGE_INCOMING_FORMAT =
                "configuration.chat.direct_messages.incoming_format";
        public static final String DIRECT_MESSAGE_OUTGOING_FORMAT =
                "configuration.chat.direct_messages.outgoing_format";
        public static final String DIRECT_MESSAGE_SPY_FORMAT = "configuration.chat.direct_messages.spy_format";
        public static final String DIRECT_MESSAGE_LIMITER_MESSAGES_AMOUNT =
                "configuration.chat.direct_messages.limiter.time_amount_millis";
        public static final String DIRECT_MESSAGE_LIMITER_TIME_AMOUNT =
                "configuration.chat.direct_messages.limiter.time_amount_millis";
        public static final String DIRECT_MESSAGE_COMPARATOR_MIN_LENGTH =
                "configuration.chat.direct_messages.comparator.minimum_length";
        public static final String DIRECT_MESSAGE_COMPARATOR_THRESHOLD =
                "configuration.chat.direct_messages.comparator.minimum_char_differences";

        // Paper
        public static final String PAPER_SERVER_NAME_ON_PROXY = "configuration.server_name_on_the_proxy";
        public static final String DATABASE_MONGO_PAPER_SERVER_PROFILES_COLLECTION =
                "configuration.database.server_profiles_collection";

        public static final String GLOBAL_CHAT_FORMAT = "configuration.chat.global.format";
        public static final String GLOBAL_CHAT_PREFIX = "configuration.chat.global.prefix";
        public static final String GLOBAL_CHAT_LIMITER_MESSAGES_AMOUNT =
                "configuration.chat.global.limiter.messages_amount";
        public static final String GLOBAL_CHAT_LIMITER_TIME_AMOUNT =
                "configuration.chat.global.limiter.time_amount_millis";
        public static final String GLOBAL_CHAT_COMPARATOR_MIN_LENGTH =
                "configuration.chat.global.comparator.minimum_length";
        public static final String GLOBAL_CHAT_COMPARATOR_THRESHOLD =
                "configuration.chat.global.comparator.minimum_char_differences";
        public static final String LOCAL_CHAT_FORMAT = "configuration.chat.local.format";
        public static final String LOCAL_CHAT_SPY_FORMAT = "configuration.chat.local.spy_format";
        public static final String LOCAL_CHAT_RADIUS = "configuration.chat.local.radius";
        public static final String LOCAL_CHAT_LIMITER_MESSAGES_AMOUNT =
                "configuration.chat.local.limiter.messages_amount";
        public static final String LOCAL_CHAT_LIMITER_TIME_AMOUNT =
                "configuration.chat.local.limiter.time_amount_millis";
        public static final String LOCAL_CHAT_COMPARATOR_MIN_LENGTH =
                "configuration.chat.local.comparator.minimum_length";
        public static final String LOCAL_CHAT_COMPARATOR_THRESHOLD =
                "configuration.chat.local.comparator.minimum_char_differences";

        public static final String TELEPORT_COOLDOWN = "configuration.teleport.cooldown_in_seconds";
        public static final String TELEPORT_DELAY = "configuration.teleport.delay_in_ticks";

        public static final String IN_COMBAT_SHOULD_USE_TOTEM = "configuration.combat.should_use_totem_on_logout";
        public static final String IN_COMBAT_MOB_TICKS = "configuration.combat.time_in_combat_vs_mobs_ticks";
        public static final String IN_COMBAT_PLAYER_TICKS = "configuration.combat.time_in_combat_vs_players_ticks";
        public static final String IN_COMBAT_BLOCKED_COMMANDS = "configuration.combat.blocked_commands";


        /* Language */
        // Common
        public static final String LANG_UNKNOWN_PLAYER_NAME = "language.unknown_player_name";
        public static final String LANG_CONSOLE_NAME = "language.unknown_player_name";
        public static final String LANG_NO_PERMISSION = "language.no_permission";
        public static final String LANG_PLAYER_NOT_FOUND = "language.player_not_found";
        public static final String LANG_MANY_PLAYERS_FOUND = "language.player_not_found";

        public static final String LANG_CONSOLE_CANT_EXECUTE_COMMAND =
                "language.commands.console_can_not_execute_command";
        public static final String LANG_GENERIC_COMMAND_ERROR = "language.commands.generic_command_error";

        public static final String LANG_COULD_NOT_RETRIEVE_PROFILE =
                "language.kick_messages.could_not_retrieve_profile";
        public static final String LANG_COULD_NOT_SAVE_PROFILE = "language.kick_messages.could_not_save_profile";

        // Velocity
        public static final String LANG_OFFLINE_PLAYERS_ONLY = "language.commands.offline_players_only";

        public static final String LANG_LOGIN_HELP = "language.commands.login.help";
        public static final String LANG_LOGIN_ALREADY_LOGGED_IN = "language.commands.login.already_logged_in";
        public static final String LANG_LOGIN_MUST_REGISTER_FIRST = "language.commands.login.must_register_first";
        public static final String LANG_LOGIN_INCORRECT_PIN = "language.commands.login.incorrect_pin";
        public static final String LANG_LOGIN_SUCCESSFULLY_LOGGED_IN = "language.commands.login.successfully_logged_in";

        public static final String LANG_REGISTER_INVALID_PINS = "language.commands.register.invalid_pin_input";
        public static final String LANG_REGISTER_PINS_DO_NOT_MATCH = "language.commands.register.pins_do_not_match";
        public static final String LANG_REGISTER_PIN_FORMATTING_FAILED =
                "language.commands.register.pin_formatting_failed";
        public static final String LANG_REGISTER_HELP = "language.commands.register.help";
        public static final String LANG_REGISTER_TRY_LOGIN_INSTEAD = "language.commands.register.use_login_instead";
        public static final String LANG_REGISTER_TRY_CHANGE_PIN_INSTEAD =
                "language.commands.register.use_change_pin_instead";

        public static final String LANG_CHANGE_PIN_HELP = "language.commands.change_pin.help";
        public static final String LANG_CHANGE_PIN_PINS_DO_NOT_MATCH =
                "language.commands.change_pin.new_pin_do_not_match";

        public static final String LANG_REPORT_UNKNOWN_REASON = "language.commands.report.unknown_report_reason";
        public static final String LANG_REPORT_NO_EXPIRATION_DATE =
                "language.commands.report.unknown_report_expiration_date";
        public static final String LANG_REPORT_YOU_HAVE_BEEN_MUTED = "language.commands.report.muted";
        public static final String LANG_REPORT_HELP = "language.commands.report.help";
        public static final String LANG_REPORT_PLAYER_EXEMPT =
                "language.commands.report.player_is_exempt_from_punishment";
        public static final String LANG_REPORT_COULD_NOT_SAVE_REPORT = "language.commands.report.could_not_save_report";
        public static final String LANG_REPORT_SUB_COMMAND_PLAYER_REASON_HELP =
                "language.commands.report.help_subcommand_player_reason";
        public static final String LANG_REPORT_UNREPORT_HELP = "language.commands.report.unreport_help";
        public static final String LANG_REPORT_UNREPORT_NO_ENTRY =
                "language.commands.report.unreport_no_entry_for_player";
        public static final String LANG_REPORT_UNREPORT_SUCCESSFULLY = "language.commands.report.unreport_successful";
        public static final String LANG_REPORT_HISTORY_HELP = "language.commands.report.help_history_subcommand";
        public static final String LANG_REPORT_HISTORY_NO_ENTRIES = "language.commands.report.history_no_entries";
        public static final String LANG_REPORT_HISTORY_BASE = "language.commands.report.history_base_list";
        public static final String LANG_REPORT_HISTORY_ENTRY_BANNED = "language.commands.report.history_entry_banned";
        public static final String LANG_REPORT_HISTORY_ENTRY_MUTED = "language.commands.report.history_entry_muted";
        public static final String LANG_REPORT_HISTORY_ENTRY_KICKED = "language.commands.report.history_entry_kicked";

        public static final String LANG_CHAT_TOO_FREQUENT = "language.chat.messages_too_frequent";
        public static final String LANG_CHAT_SIMILAR = "language.chat.messages_are_similar";

        public static final String LANG_DIRECT_MESSAGE_HELP = "language.commands.message.help";
        public static final String LANG_DIRECT_MESSAGE_REPLY_HELP = "language.commands.reply.help";
        public static final String LANG_DIRECT_MESSAGE_NO_RECIPIENT = "language.commands.reply.recipient_not_found";
        public static final String LANG_DIRECT_MESSAGE_RECIPIENT_LEFT = "language.commands.reply.recipient_left";

        public static final String LANG_KICKED = "language.kick_messages.kicked";
        public static final String LANG_BANNED = "language.kick_messages.banned";
        public static final String LANG_COULD_NOT_CHECK_PLAYER_TYPE =
                "language.kick_messages.could_not_check_player_type";
        public static final String LANG_COULD_NOT_CHECK_MOJANG_USERNAME =
                "language.kick_messages.could_not_check_mojang_username";
        public static final String LANG_FLOODGATE_PREFIX_ON_JAVA_PLAYER =
                "language.kick_messages.floodgate_prefix_on_java_player";
        public static final String LANG_NO_LOGIN_SERVER_AVAILABLE = "language.kick_messages.login_server_unavailable";
        public static final String LANG_LOGIN_TRANSFER_UNAVAILABLE = "language.kick_messages.lobby_server_unavailable";


        // Paper
        public static final String LANG_TELEPORT_PROGRESS_BAR_TITLE = "language.teleport.progress_bar_title";
        public static final String LANG_TELEPORT_SUCCESS = "language.teleport.success";
        public static final String LANG_TELEPORT_FAIL = "language.teleport.failed";
        public static final String LANG_TELEPORT_DESTINATION_UNAVAILABLE = "language.teleport.destination_unavailable";

        public static final String LANG_COMBAT_PROGRESS_BAR_TITLE = "language.combat.progress_bar_title";
        public static final String LANG_COMBAT_BLOCKED_COMMAND = "language.combat.this_command_is_blocked_in_combat";
        public static final String LANG_COMBAT_LAST_DEATH_LOCATION = "language.combat.show_last_death_location";
        public static final String LANG_COMBAT_LAST_DEATH_LOCATION_WORLD =
                "language.combat.show_last_death_location.with_world";


        private Constants() {
        }

    }
}
