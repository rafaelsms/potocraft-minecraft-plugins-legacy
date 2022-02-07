package com.rafaelsms.potocraft.universalchat;


import com.rafaelsms.potocraft.universalchat.commands.MessageCommand;
import com.rafaelsms.potocraft.universalchat.commands.ReplyCommand;
import com.rafaelsms.potocraft.universalchat.listeners.ChatListener;
import com.rafaelsms.potocraft.universalchat.listeners.UserManager;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class UniversalChatPlugin extends Plugin {

    private final @NotNull Configuration configuration;
    private final @NotNull UserManager userManager;

    public UniversalChatPlugin() throws IOException {
        this.configuration = new Configuration(getDataFolder().toPath());
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        // Register listeners
        getProxy().getPluginManager().registerListener(this, userManager);
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new MessageCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this));

        logger().info("UniversalChat enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister listeners
        getProxy().getPluginManager().unregisterListeners(this);

        logger().info("UniversalChat disabled!");
    }

    public Logger logger() {
        return getSLF4JLogger();
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull UserManager getUserManager() {
        return userManager;
    }
}
