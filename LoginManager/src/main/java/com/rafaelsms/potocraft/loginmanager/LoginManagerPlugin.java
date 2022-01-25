package com.rafaelsms.potocraft.loginmanager;

import com.google.inject.Inject;
import com.rafaelsms.potocraft.loginmanager.commands.*;
import com.rafaelsms.potocraft.loginmanager.listeners.*;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "loginmanager",
        name = "LoginManager",
        version = "0.1",
        url = "https://potocraft.com/",
        authors = {"rafaelsms"},
        dependencies = {@Dependency(id = "floodgate"), @Dependency(id = "luckperms", optional = true)})
public class LoginManagerPlugin {

    private final @NotNull ProxyServer server;
    private final @NotNull Logger logger;
    private final @NotNull Configuration configuration;
    private final @NotNull Database database;

    @Inject
    public LoginManagerPlugin(@NotNull ProxyServer server, @NotNull Logger logger, @DataDirectory Path dataDirectory)
            throws IOException, com.rafaelsms.potocraft.database.Database.DatabaseException {
        this.server = server;
        this.logger = logger;
        this.configuration = new Configuration(dataDirectory);
        this.database = new Database(this);
    }

    @Subscribe
    private void onInitialization(ProxyInitializeEvent event) {
        // Assert Floodgate API is available
        if (FloodgateApi.getInstance() == null) {
            logger.error("Floodgate API is a required dependency!");
            server.shutdown();
            return;
        }

        // Register listeners
        getServer().getEventManager().register(this, new LoggedOffPlayerListener(this));
        getServer().getEventManager().register(this, new OfflineCheckerListener(this));
        getServer().getEventManager().register(this, new ProfileUpdater(this));
        getServer().getEventManager().register(this, new ReportsCheckerListener(this));
        getServer().getEventManager().register(this, new RedirectPlayerListener(this));
        getServer().getEventManager().register(this, new PlayerListListener(this));
        getServer().getEventManager().register(this, new KickListener(this));

        // Register commands
        CommandManager commandManager = getServer().getCommandManager();
        commandManager.register("login", new LoginCommand(this), "l", "log");
        commandManager.register("registrar", new RegisterCommand(this), "reg", "register", "cadastrar");
        commandManager.register("mudarsenha", new ChangePinCommand(this), "changepin", "changepassword", "mudarpin");
        commandManager.register("unban", new UnbanCommand(this), "desbanir");
        commandManager.register("ban", new BanCommand(this), "banir");
        commandManager.register("tempban", new TemporaryBanCommand(this), "temporaryban", "banirtemp");
        commandManager.register("mute", new MuteCommand(this), "silenciar", "silencio");
        commandManager.register("unmute", new UnmuteCommand(this), "desilenciar");
        commandManager.register("kick", new KickCommand(this), "expulsar");
        commandManager.register("list", new ListCommand(this), "lista", "jogadores", "players", "online");
        commandManager.register("seen", new SeenCommand(this), "info", "search", "buscar", "history", "historico");

        getLogger().info("LoginManager enabled!");
    }

    @Subscribe
    private void onReload(ProxyReloadEvent event) {
        try {
            this.configuration.loadConfiguration();
        } catch (IOException exception) {
            getLogger().warn("Failed to load configuration:", exception);
        }
    }

    @Subscribe
    private void onShutdown(ProxyShutdownEvent event) {
        // Unregister listeners
        getServer().getEventManager().unregisterListeners(this);
        // Stop database
        this.database.close();

        getLogger().info("LoginManager disabled!");
    }

    public @NotNull ProxyServer getServer() {
        return server;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull Database getDatabase() {
        return database;
    }
}
