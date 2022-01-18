package com.rafaelsms.potocraft.hardcore;

import com.rafaelsms.potocraft.hardcore.listeners.BannedChecker;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class HardcorePlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;

    public HardcorePlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
    }

    @Override
    public void onEnable() {
        // Register listeners
        getServer().getPluginManager().registerEvents(new BannedChecker(this), this);

        logger().info("Hardcore plugin enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("Hardcore plugin disabled!");
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull Database getDatabase() {
        return database;
    }

    public Logger logger() {
        return getSLF4JLogger();
    }
}
