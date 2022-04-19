package com.rafaelsms.discordbot;

import com.rafaelsms.potocraft.util.YamlFile;
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
        return getOrThrow("configuration.token");
    }

    public String getBotPlayingActivity() {
        return getOrThrow("configuration.bot_playing_activity");
    }

    public String getOperatorRoleName() {
        return getOrThrow("configuration.operator_role_name");
    }

    public String getRemovedMessagesCategoryName() {
        return getOrThrow("configuration.removed_messages.category_name");
    }

    public String getRemovedMessagesChannelName() {
        return getOrThrow("configuration.removed_messages.channel_name");
    }

    public Duration getRemovedMessagesCacheTime() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.removed_messages.cached_time_in_seconds")));
    }

    public String getTicketCategoryName() {
        return getOrThrow("configuration.tickets.category_name");
    }

    public String getTicketLobbyChannelName() {
        return getOrThrow("configuration.tickets.lobby_channel_name");
    }

    public boolean isLengthLimiterEnabled() {
        return Objects.requireNonNull(getOrThrow("configuration.message_length_limiter.enabled"));
    }

    public boolean shouldLengthyMessagesBeRemoved() {
        return Objects.requireNonNull(getOrThrow("configuration.message_length_limiter.should_be_removed"));
    }

    public int getMaximumMessageLength() {
        return Objects.requireNonNull(getIntOrNull("configuration.message_length_limiter.maximum_length"));
    }

    public int getMaximumNewLines() {
        return Objects.requireNonNull(getIntOrNull("configuration.message_length_limiter.maximum_new_line_count"));
    }

    public int getMaximumEmojiCount() {
        return Objects.requireNonNull(getIntOrNull("configuration.message_length_limiter.maximum_emoji_count"));
    }

    public Duration getExceededLengthTimeoutDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.message_length_limiter.user_timeout_seconds")));
    }

    public boolean isMentionsShouldBeRemoved() {
        return Objects.requireNonNull(getOrThrow("configuration.mentions.should_be_removed"));
    }

    public boolean isMentionsShouldBeWarned() {
        return Objects.requireNonNull(getOrThrow("configuration.mentions.should_be_warned"));
    }

    public Duration getMentionsWarningDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.mentions.warn_message_duration_seconds")));
    }

    public Duration getMentioningTimeout() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull("configuration.mentions.user_timeout_seconds")));
    }

    public boolean isCursingBlockerEnabled() {
        return Objects.requireNonNull(getOrThrow("configuration.blocked_words.enabled"));
    }

    public Duration getCursedTimeout() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.blocked_words.user_timeout_seconds")));
    }

    public List<String> getBlockedWords() {
        return getOrThrow("configuration.blocked_words.word_list");
    }

    public String getOpenTicketMessage() {
        return getOrThrow("language.tickets.open_ticket_message");
    }

    public String getOpenedTicketMessage() {
        return getOrThrow("language.tickets.ticket_opened_instructions_message");
    }

    public String getTicketChannelCreatedMessage() {
        return getOrThrow("language.tickets.ticket_channel_created_message");
    }

    public String getTicketClosingMessage() {
        return getOrThrow("language.tickets.ticket_closing_message");
    }

    public String getTicketChannelAlreadyExists() {
        return getOrThrow("language.tickets.ticket_channel_already_exists");
    }

    public String getMentionWarningMessage() {
        return getOrThrow("language.message_mentions_user_directly");
    }
}
