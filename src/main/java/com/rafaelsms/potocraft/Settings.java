package com.rafaelsms.potocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rafaelsms.potocraft.util.Util;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Settings {

    private final Gson jsonConfig = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    protected final Map<String, Object> configuration = Collections.synchronizedMap(new LinkedHashMap<>());

    protected final @NotNull Plugin plugin;

    protected Settings(@NotNull Plugin plugin) throws Exception {
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

    protected void setDefaults() {
        setDefault(Constants.DEBUG_MESSAGES_ENABLED, false);

        setDefault(Constants.DATABASE_MONGO_URI, "mongodb://localhost:27017");
        setDefault(Constants.DATABASE_MONGO_DATABASE, "potocraftDb");
        setDefault(Constants.DATABASE_FAIL_FATAL, true);
        setDefault(Constants.DATABASE_FAIL_PRINT_STACK, true);

        setDefault(Constants.LANG_CONSOLE_CANT_EXECUTE_COMMAND, "&cConsole não pode executar este comando.");
        setDefault(Constants.LANG_GENERIC_COMMAND_ERROR, "&cFalha ao executar comando.");
        setDefault(Constants.LANG_COULD_NOT_RETRIEVE_PROFILE, "&cFalha ao acessar perfil.");
        setDefault(Constants.LANG_COULD_NOT_SAVE_PROFILE, "&cFalha ao salvar perfil.");
    }

    public boolean isDebugMessagesEnabled() {
        return get(Constants.DEBUG_MESSAGES_ENABLED);
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

    protected Component getLang(@NotNull String key) {
        try {
            return Util.getLang(get(key));
        } catch (Exception exception) {
            plugin.logger().warn("Couldn't get language for \"%s\": %s".formatted(key, exception.getLocalizedMessage()));
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

    protected static final class Constants {

        /* Settings */
        // Common
        public static final String DEBUG_MESSAGES_ENABLED = "configuration.debug_messages_enabled";

        public static final String DATABASE_MONGO_URI = "configuration.database.mongo_uri";
        public static final String DATABASE_MONGO_DATABASE = "configuration.database.mongo_database";
        public static final String DATABASE_FAIL_FATAL = "configuration.database.database_fail_shutdowns_server";
        public static final String DATABASE_FAIL_PRINT_STACK = "configuration.database.database_fail_prints_stack_trace";

        // Velocity
        public static final String LOGIN_MAX_LOGIN_DURATION_SECONDS = "configuration.login.max_time_since_last_login_to_auto_login_seconds";
        public static final String LOGIN_SERVER_NAME = "configuration.login.unauthenticated_server";
        public static final String LOBBY_SERVER_NAME = "configuration.login.lobby_server";
        public static final String KICK_IF_LOBBY_UNAVAILABLE = "configuration.login.kick_if_lobby_server_unavailable";

        // Paper
        public static final String PAPER_SERVER_NAME_ON_PROXY = "configuration.server_name_on_the_proxy";


        /* Language */
        // Common
        public static final String LANG_CONSOLE_CANT_EXECUTE_COMMAND = "language.commands.console_can_not_execute_command";
        public static final String LANG_GENERIC_COMMAND_ERROR = "language.commands.generic_command_error";

        public static final String LANG_COULD_NOT_RETRIEVE_PROFILE = "language.kick_messages.could_not_retrieve_profile";
        public static final String LANG_COULD_NOT_SAVE_PROFILE = "language.kick_messages.could_not_save_profile";

        // Velocity
        public static final String LANG_OFFLINE_PLAYERS_ONLY = "language.commands.offline_players_only";
        public static final String LANG_LOGIN_HELP = "language.commands.login.help";
        public static final String LANG_LOGIN_ALREADY_LOGGED_IN = "language.commands.login.already_logged_in";
        public static final String LANG_LOGIN_MUST_REGISTER_FIRST = "language.commands.login.must_register_first";
        public static final String LANG_LOGIN_INCORRECT_PIN = "language.commands.login.incorrect_pin";
        public static final String LANG_LOGIN_SUCCESSFULLY_LOGGED_IN = "language.commands.login.successfully_logged_in";

        public static final String LANG_COULD_NOT_CHECK_PLAYER_TYPE = "language.kick_messages.could_not_check_player_type";
        public static final String LANG_COULD_NOT_CHECK_MOJANG_USERNAME = "language.kick_messages.could_not_check_mojang_username";
        public static final String LANG_FLOODGATE_PREFIX_ON_JAVA_PLAYER = "language.kick_messages.floodgate_prefix_on_java_player";
        public static final String LANG_NO_LOGIN_SERVER_AVAILABLE = "language.kick_messages.login_server_unavailable";
        public static final String LANG_LOGIN_TRANSFER_UNAVAILABLE = "language.kick_messages.lobby_server_unavailable";

        private Constants() {
        }

    }
}
