package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.blockprotection.listeners.ProtectionListener;
import com.rafaelsms.potocraft.blockprotection.listeners.UserManager;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class BlockProtectionPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;

    public BlockProtectionPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(userManager, this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        logger().info("BlockProtection enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("BlockProtection disabled!");
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull Database getDatabase() {
        return database;
    }

    public @NotNull UserManager getUserManager() {
        return userManager;
    }

    public Logger logger() {
        return getSLF4JLogger();
    }
}
