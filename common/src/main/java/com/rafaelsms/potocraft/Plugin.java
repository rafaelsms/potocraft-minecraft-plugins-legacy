package com.rafaelsms.potocraft;

import com.rafaelsms.potocraft.configuration.PluginConfiguration;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.pojo.ServerProfile;
import org.slf4j.Logger;

import java.io.File;

public interface Plugin {

    Logger logger();

    PluginConfiguration getConfiguration();

    BaseDatabase<ServerProfile> getDatabase();

    void shutdown();

    File getDataFolder();
}
