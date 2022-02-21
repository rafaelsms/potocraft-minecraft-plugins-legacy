package com.rafaelsms.potocraft.combatserver;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class Configuration extends YamlFile {

    public Configuration(@NotNull CombatServerPlugin plugin) throws IOException {
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

    public String getMongoPlayerCollectionName() {
        return get("configuration.database.player_collection_name");
    }

    public long getSaveProfileTaskTimer() {
        return Objects.requireNonNull(getLong("configuration.database.save_player_timer_task_ticks"));
    }

    public Component getFailedToRetrieveProfile() {
        return TextUtil.toComponent(get("language.failed_to_retrieve_profile"));
    }
}
