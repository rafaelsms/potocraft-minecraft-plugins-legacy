package com.rafaelsms.potocraft.combatserver;

import com.rafaelsms.potocraft.combatserver.commands.SpawnCommand;
import com.rafaelsms.potocraft.combatserver.listeners.CombatDrops;
import com.rafaelsms.potocraft.combatserver.listeners.EquipmentManager;
import com.rafaelsms.potocraft.combatserver.listeners.UserManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class CombatServerPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;

    private final @NotNull UserManager userManager;

    public CombatServerPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(userManager.getListener(), this);
        getServer().getPluginManager().registerEvents(new CombatDrops(this), this);
        getServer().getPluginManager().registerEvents(new EquipmentManager(this), this);
        //getServer().getPluginManager().registerEvents(new TestEquipmentListener(this), this);

        registerCommand("spawn", new SpawnCommand(this));

        logger().info("CombatServer enabled");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        logger().info("CombatServer disabled");
    }

    public @NotNull Logger logger() {
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

    private void registerCommand(@NotNull String commandName, @NotNull CommandExecutor commandExecutor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(commandName);
        if (pluginCommand == null) {
            logger().error("Failed to register command {}: it doesn't exist", commandName);
            return;
        }
        pluginCommand.setExecutor(commandExecutor);
    }
}
