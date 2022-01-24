package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.serverutility.commands.*;
import com.rafaelsms.potocraft.serverutility.listeners.HideMessagesListener;
import com.rafaelsms.potocraft.serverutility.listeners.VanishManager;
import com.rafaelsms.potocraft.serverutility.listeners.WorldGameRuleApplier;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class ServerUtilityPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull VanishManager manager;

    public ServerUtilityPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.manager = new VanishManager(this);
    }

    @Override
    public void onEnable() {
        // Register listeners
        getServer().getPluginManager().registerEvents(manager, this);
        getServer().getPluginManager().registerEvents(new WorldGameRuleApplier(this), this);
        getServer().getPluginManager().registerEvents(new HideMessagesListener(this), this);

        // Register commands
        registerCommand("anvil", new AnvilCommand(this));
        registerCommand("enderchest", new EnderchestCommand(this));
        registerCommand("workbench", new WorkbenchCommand(this));
        registerCommand("playertime", new PlayerTimeCommand(this));
        registerCommand("playerweather", new PlayerWeatherCommand(this));
        registerCommand("vanish", new VanishCommand(this));

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

    public @NotNull VanishManager getVanishManager() {
        return manager;
    }

    public Logger logger() {
        return getSLF4JLogger();
    }

    private void registerCommand(@NotNull String command, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(command);
        if (pluginCommand == null) {
            throw new IllegalStateException("Couldn't find command %s. Make sure it is on plugin.yml".formatted(command));
        }
        pluginCommand.setExecutor(executor);
    }
}