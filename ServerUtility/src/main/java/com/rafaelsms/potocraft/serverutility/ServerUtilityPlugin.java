package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.serverutility.listeners.WorldGameRuleApplier;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class ServerUtilityPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;

    public ServerUtilityPlugin() throws IOException {
        this.configuration = new Configuration(this);
    }

    @Override
    public void onEnable() {
        // Register listeners
        getServer().getPluginManager().registerEvents(new WorldGameRuleApplier(this), this);

        logger().info("ServerUtility enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("ServerUtility disabled!");
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public Logger logger() {
        return getSLF4JLogger();
    }
}
