package com.rafaelsms.potocraft.combatserver;

import com.rafaelsms.potocraft.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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

}
