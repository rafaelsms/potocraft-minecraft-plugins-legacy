package com.rafaelsms.potocraft.serverprofile;

import com.rafaelsms.potocraft.serverprofile.commands.*;
import com.rafaelsms.potocraft.serverprofile.listeners.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class ServerProfilePlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;

    public ServerProfilePlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        // Register listeners
        getServer().getPluginManager().registerEvents(userManager, this);
        getServer().getPluginManager().registerEvents(new ChatFormatter(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new StatisticsListener(this), this);
        getServer().getPluginManager().registerEvents(new DisplayNameSetter(this), this);

        // Register commands
        registerCommand("voltar", new BackCommand(this));
        registerCommand("criarcasa", new CreateHomeCommand(this));
        registerCommand("apagarcasa", new DeleteHomeCommand(this));
        registerCommand("casa", new HomeCommand(this));
        registerCommand("criarportal", new CreateWarpCommand(this));
        registerCommand("apagarportal", new DeleteWarpCommand(this));
        registerCommand("portal", new WarpCommand(this));
        registerCommand("teleporteaceitar", new TeleportAcceptCommand(this));
        registerCommand("teleporterecusar", new TeleportDenyCommand(this));
        registerCommand("teleporte", new TeleportCommand(this));
        registerCommand("teleporteaqui", new TeleportHereCommand(this));
        registerCommand("mundo", new WorldCommand(this));

        logger().info("ServerProfile enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("ServerProfile disabled!");
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

    private void registerCommand(@NotNull String name, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            throw new IllegalStateException("Command couldn't be registered: %s".formatted(name));
        }
        pluginCommand.setExecutor(executor);
    }
}
