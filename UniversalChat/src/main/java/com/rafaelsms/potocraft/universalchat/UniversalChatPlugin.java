package com.rafaelsms.potocraft.universalchat;


import com.google.inject.Inject;
import com.rafaelsms.potocraft.universalchat.commands.MessageCommand;
import com.rafaelsms.potocraft.universalchat.commands.ReplyCommand;
import com.rafaelsms.potocraft.universalchat.listeners.ChatListener;
import com.rafaelsms.potocraft.universalchat.listeners.UserManager;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "universalchat",
        name = "UniversalChat",
        version = "0.1",
        url = "https://potocraft.com/",
        authors = {"rafaelsms"})
public class UniversalChatPlugin {

    private final @NotNull ProxyServer server;
    private final @NotNull Logger logger;
    private final @NotNull Configuration configuration;
    private final @NotNull UserManager userManager;

    @Inject
    public UniversalChatPlugin(@NotNull ProxyServer server, @NotNull Logger logger, @DataDirectory Path dataDirectory)
            throws IOException {
        this.server = server;
        this.logger = logger;
        this.configuration = new Configuration(dataDirectory);
        this.userManager = new UserManager(this);
    }

    @Subscribe
    private void onInitialization(ProxyInitializeEvent event) {
        // Register listeners
        getServer().getEventManager().register(this, userManager);
        getServer().getEventManager().register(this, new ChatListener(this));

        // Register commands
        CommandManager commandManager = getServer().getCommandManager();
        commandManager.register("mensagem",
                                new MessageCommand(this),
                                "message",
                                "msg",
                                "tell",
                                "dm",
                                "pm",
                                "w",
                                "whisper");
        commandManager.register("responder", new ReplyCommand(this), "r", "reply");

        getLogger().info("UniversalChat enabled!");
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

        getLogger().info("UniversalChat disabled!");
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

    public @NotNull UserManager getUserManager() {
        return userManager;
    }
}
