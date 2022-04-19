package com.rafaelsms.potocraft.loginmanager;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.commands.BanCommand;
import com.rafaelsms.potocraft.loginmanager.commands.ChangePasswordCommand;
import com.rafaelsms.potocraft.loginmanager.commands.KickCommand;
import com.rafaelsms.potocraft.loginmanager.commands.ListCommand;
import com.rafaelsms.potocraft.loginmanager.commands.LoginCommand;
import com.rafaelsms.potocraft.loginmanager.commands.MuteCommand;
import com.rafaelsms.potocraft.loginmanager.commands.PingCommand;
import com.rafaelsms.potocraft.loginmanager.commands.RegisterCommand;
import com.rafaelsms.potocraft.loginmanager.commands.SeenCommand;
import com.rafaelsms.potocraft.loginmanager.commands.TemporaryBanCommand;
import com.rafaelsms.potocraft.loginmanager.commands.UnbanCommand;
import com.rafaelsms.potocraft.loginmanager.commands.UnmuteCommand;
import com.rafaelsms.potocraft.loginmanager.listeners.LoggedOffPlayerListener;
import com.rafaelsms.potocraft.loginmanager.listeners.OfflineCheckerListener;
import com.rafaelsms.potocraft.loginmanager.listeners.PlayerTypeManager;
import com.rafaelsms.potocraft.loginmanager.listeners.ProfileUpdater;
import com.rafaelsms.potocraft.loginmanager.listeners.RedirectPlayerListener;
import com.rafaelsms.potocraft.loginmanager.listeners.ReportsCheckerListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class LoginManagerPlugin extends Plugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull PlayerTypeManager playerTypeManager;

    public LoginManagerPlugin() throws IOException, DatabaseException {
        this.configuration = new Configuration(getDataFolder().toPath());
        this.database = new Database(this);
        this.playerTypeManager = new PlayerTypeManager(this);
    }

    @Override
    public void onEnable() {
        // Assert Floodgate API is available
        if (FloodgateApi.getInstance() == null) {
            logger().error("Floodgate API is a required dependency!");
            return;
        }

        // Register listeners
        getProxy().getPluginManager().registerListener(this, playerTypeManager);
        getProxy().getPluginManager().registerListener(this, new OfflineCheckerListener(this));
        getProxy().getPluginManager().registerListener(this, new LoggedOffPlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new ProfileUpdater(this));
        getProxy().getPluginManager().registerListener(this, new ReportsCheckerListener(this));
        getProxy().getPluginManager().registerListener(this, new RedirectPlayerListener(this));

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new LoginCommand(this));
        getProxy().getPluginManager().registerCommand(this, new RegisterCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ChangePasswordCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new TemporaryBanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new KickCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ListCommand(this));
        getProxy().getPluginManager().registerCommand(this, new SeenCommand(this));
        getProxy().getPluginManager().registerCommand(this, new PingCommand(this));

        logger().info("LoginManager enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister listeners
        getProxy().getPluginManager().unregisterListeners(this);
        // Stop database
        this.database.close();

        logger().info("LoginManager disabled!");
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull Database getDatabase() {
        return database;
    }

    public @NotNull PlayerTypeManager getPlayerTypeManager() {
        return playerTypeManager;
    }

    public Logger logger() {
        return getSLF4JLogger();
    }

    public ScheduledTask runAsync(@NotNull Runnable task) {
        return getProxy().getScheduler().runAsync(this, task);
    }
}
