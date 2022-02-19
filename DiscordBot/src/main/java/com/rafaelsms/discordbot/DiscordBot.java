package com.rafaelsms.discordbot;

import com.rafaelsms.discordbot.listeners.BlockedWordsListener;
import com.rafaelsms.discordbot.listeners.RemovedMessagesListener;
import com.rafaelsms.discordbot.listeners.SetActivityListener;
import com.rafaelsms.discordbot.listeners.TicketListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;

public class DiscordBot {

    private final @NotNull Configuration configuration;
    private final @NotNull Logger logger;
    private final @NotNull JDA jda;

    DiscordBot() throws Exception {
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        this.logger = LoggerFactory.getLogger(getClass());

        try {
            Path configPath =
                    Path.of(DiscordBot.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
            this.configuration = new Configuration(configPath.toFile(), "config.yml");
        } catch (IOException exception) {
            this.logger.error("Failed to initialize configuration file:", exception);
            throw exception;
        }
        try {
            String token = configuration.getToken();
            this.jda = JDABuilder.createDefault(token).build().awaitReady();
        } catch (LoginException exception) {
            this.logger.error("Invalid Discord token: {}", exception.getMessage());
            throw exception;
        }
        this.logger.info("Bot connected, adding listeners...");
        this.jda.addEventListener(new SetActivityListener(this));
        this.jda.addEventListener(new BlockedWordsListener(this));
        this.jda.addEventListener(new TicketListener(this));
        this.jda.addEventListener(new RemovedMessagesListener(this));
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull JDA getJda() {
        return jda;
    }
}
