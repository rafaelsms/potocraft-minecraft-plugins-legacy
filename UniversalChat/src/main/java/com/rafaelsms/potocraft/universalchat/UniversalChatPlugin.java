package com.rafaelsms.potocraft.universalchat;


import com.rafaelsms.potocraft.player.BlockedWordsChecker;
import com.rafaelsms.potocraft.universalchat.commands.MessageCommand;
import com.rafaelsms.potocraft.universalchat.commands.ReplyCommand;
import com.rafaelsms.potocraft.universalchat.listeners.ChatListener;
import com.rafaelsms.potocraft.universalchat.listeners.UserManager;
import com.rafaelsms.potocraft.universalchat.tasks.BroadcastTask;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class UniversalChatPlugin extends Plugin implements com.rafaelsms.potocraft.Logger {

    private final @NotNull Configuration configuration;
    private final @NotNull UserManager userManager;
    private final @NotNull BlockedWordsChecker blockedWordsChecker;

    public UniversalChatPlugin() throws IOException {
        this.configuration = new Configuration(getDataFolder().toPath());
        this.userManager = new UserManager(this);
        this.blockedWordsChecker = new BlockedWordsChecker(logger(), configuration.getBlockedWordsList());
    }

    @Override
    public void onEnable() {
        // Register listeners
        getProxy().getPluginManager().registerListener(this, userManager);
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new MessageCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this));

        // Register tasks
        Duration period = configuration.getBroadcastPeriod();
        getProxy().getScheduler()
                  .schedule(this, new BroadcastTask(this), period.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);

        info("UniversalChat enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister listeners and tasks
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getScheduler().cancel(this);

        info("UniversalChat disabled!");
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull UserManager getUserManager() {
        return userManager;
    }

    public @NotNull BlockedWordsChecker getWordsChecker() {
        return blockedWordsChecker;
    }

    @Override
    public @NotNull Logger logger() {
        return getSLF4JLogger();
    }
}
