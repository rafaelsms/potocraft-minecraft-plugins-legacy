package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.blockprotection.listeners.UserManager;
import com.rafaelsms.potocraft.blockprotection.listeners.VolumeListener;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class BlockProtectionPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;
    private final @NotNull VolumeListener volumeListener;

    public BlockProtectionPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
        this.volumeListener = new VolumeListener(this);
    }

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(userManager.getListener(), this);

        logger().info("BlockProtection enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("BlockProtection disabled!");
    }

    public Logger logger() {
        return getSLF4JLogger();
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

    public @NotNull VolumeListener getProtectionManager() {
        return volumeListener;
    }

    private void registerCommand(@NotNull String commandName, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(commandName);
        assert pluginCommand != null;
        pluginCommand.setExecutor(executor);
    }
}
