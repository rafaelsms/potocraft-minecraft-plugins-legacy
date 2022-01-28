package com.rafaelsms.potocraft.configuration;

import com.rafaelsms.potocraft.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PluginConfiguration extends BaseConfiguration {

    protected PluginConfiguration(@NotNull Plugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public String getMongoUri() {
        return getString(Constants.MONGO_URI);
    }

    public String getMongoDatabase() {
        return getString(Constants.MONGO_URI);
    }

    public boolean isMongoExceptionFatal() {
        return getBoolean(Constants.MONGO_IS_EXCEPTION_FATAL);
    }

}
