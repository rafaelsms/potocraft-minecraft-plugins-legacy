package com.rafaelsms.potocraft.papermc;

import com.rafaelsms.potocraft.common.CommonServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PaperServer implements CommonServer {

    private final @NotNull JavaPlugin plugin;

    PaperServer(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public File getConfigurationFile() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir())
            throw new IllegalStateException("Couldn't create data folder");
        return new File(plugin.getDataFolder(), "config.json");
    }

    @Override
    public void shutdown() {
        plugin.getServer().shutdown();
    }
}
