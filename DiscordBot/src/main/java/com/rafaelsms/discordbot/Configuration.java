package com.rafaelsms.discordbot;

import com.rafaelsms.potocraft.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class Configuration extends YamlFile {

    public Configuration(@NotNull File dataFolder, @NotNull String fileName) throws IOException {
        super(dataFolder, fileName);
    }

    public String getToken() {
        return get("configuration.token");
    }

    public String getBotPlayingActivity() {
        return get("configuration.bot_playing_activity");
    }

    public String getOperatorRoleName() {
        return get("configuration.operator_role_name");
    }

    public String getRemovedMessagesCategoryName() {
        return get("configuration.removed_messages.category_name");
    }

    public String getRemovedMessagesChannelName() {
        return get("configuration.removed_messages.channel_name");
    }

    public Duration getRemovedMessagesCacheTime() {
        return Duration.ofSeconds(Objects.requireNonNull(getLong("configuration.removed_messages.cached_time_in_seconds")));
    }

    public String getTicketCategoryName() {
        return get("configuration.tickets.category_name");
    }

    public String getTicketLobbyChannelName() {
        return get("configuration.tickets.lobby_channel_name");
    }

    public Duration getCursedTimeout() {
        return Duration.ofSeconds(Objects.requireNonNull(getLong("configuration.blocked_word_user_timeout_seconds")));
    }

    public List<String> getBlockedWords() {
        return get("configuration.blocked_words");
    }

    public String getOpenTicketMessage() {
        return get("language.tickets.open_ticket_message");
    }

    public String getOpenedTicketMessage() {
        return get("language.tickets.ticket_opened_instructions_message");
    }

    public String getTicketChannelCreatedMessage() {
        return get("language.tickets.ticket_channel_created_message");
    }

    public String getTicketClosingMessage() {
        return get("language.tickets.ticket_closing_message");
    }

    public String getTicketChannelAlreadyExists() {
        return get("language.tickets.ticket_channel_already_exists");
    }

    public String getMessageContainsBlockedWords() {
        return get("language.message_contains_blocked_words");
    }
}
