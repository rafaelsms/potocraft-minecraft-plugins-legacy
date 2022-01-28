package com.rafaelsms.potocraft.hardcore;

import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    public Configuration(@NotNull HardcorePlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        loadConfiguration();
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(Keys.MONGO_URI, "mongodb://localhost:27017");
        defaults.put(Keys.MONGO_DATABASE_NAME, "hardcoreDb");
        defaults.put(Keys.MONGO_HARDCORE_PROFILE_COLLECTION, "hardcoreProfiles");
        defaults.put(Keys.MONGO_DATABASE_FAILURE_FATAL, true);

        defaults.put(Keys.DEATH_BAN_TIME, 60 * 60 * 6); // 6 hours
        defaults.put(Keys.DATE_TIME_FORMAT, "H'h'mm");

        defaults.put(Keys.LANG_FAILED_TO_RETRIEVE_PROFILE, "&cFalha ao ler seu perfil.");
        defaults.put(Keys.LANG_BANNED_MESSAGE, """
                                               &cVocê está banido por ter morrido.
                                               &cPoderá entrar novamente às &e%expiration_date%&c.
                                               """);
        defaults.put(Keys.LANG_DATABASE_FAILURE_BAN, "Falha ao escrever seu perfil. Contate o administrador.");
        return defaults;
    }

    public String getMongoURI() {
        return get(Keys.MONGO_URI);
    }

    public String getMongoDatabaseName() {
        return get(Keys.MONGO_DATABASE_NAME);
    }

    public String getMongoProfileCollectionName() {
        return get(Keys.MONGO_HARDCORE_PROFILE_COLLECTION);
    }

    public boolean isMongoDatabaseExceptionFatal() {
        return get(Keys.MONGO_DATABASE_FAILURE_FATAL);
    }

    public Duration getDeathBanTime() {
        Integer banTime = get(Keys.DEATH_BAN_TIME);
        return Duration.of(banTime, ChronoUnit.SECONDS);
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(get(Keys.DATE_TIME_FORMAT));
    }

    public Component getFailedToRetrieveProfile() {
        return TextUtil.toComponent(get(Keys.LANG_FAILED_TO_RETRIEVE_PROFILE)).build();
    }

    public Component getBannedMessage(@NotNull ZonedDateTime expirationDate) {
        return TextUtil
                .toComponent(get(Keys.LANG_BANNED_MESSAGE))
                .replace("%expiration_date%", getDateTimeFormatter().format(expirationDate))
                .build();
    }

    public String getDatabaseFailureBanReason() {
        return get(Keys.LANG_DATABASE_FAILURE_BAN);
    }

    private final static class Keys {

        public static final String MONGO_URI = "configuration.database.mongo_uri";
        public static final String MONGO_DATABASE_NAME = "configuration.database.database_name";
        public static final String MONGO_HARDCORE_PROFILE_COLLECTION =
                "configuration.database.hardcore_profile_collection_name";
        public static final String MONGO_DATABASE_FAILURE_FATAL = "configuration.database.is_database_failure_fatal";

        public static final String DEATH_BAN_TIME = "configuration.death_ban_time_in_seconds";
        public static final String DATE_TIME_FORMAT = "configuration.death_date_time_format";

        public static final String LANG_FAILED_TO_RETRIEVE_PROFILE = "language.failed_to_retrieve_profile";
        public static final String LANG_BANNED_MESSAGE = "language.you_are_banned_for_dying";
        public static final String LANG_DATABASE_FAILURE_BAN = "language.you_are_banned_database_error";

        // Private constructor
        private Keys() {
        }
    }
}
