package com.rafaelsms.potocraft.universalchat;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Configuration extends YamlFile {

    public Configuration(@NotNull Path dataDirectory) throws IOException {
        super(dataDirectory.toFile(), "config.yml");
    }

    public Integer getLimiterMessageAmount() {
        return getInt("configuration.chat_limiter.message_amount");
    }

    public Duration getLimiterTimeAmount() {
        return Duration.ofMillis(Objects.requireNonNull(getLong("configuration.chat_limiter.time_amount_millis")));
    }

    public Integer getLimiterMinMessageLength() {
        return getInt("configuration.chat_limiter.min_message_length");
    }

    public Integer getComparatorMinLength() {
        return getInt("configuration.comparator.min_length_to_compare");
    }

    public Integer getComparatorMinDifferences() {
        return getInt("configuration.comparator.min_char_differences");
    }

    public @NotNull BaseComponent[] getUniversalChatFormat(@NotNull ProxiedPlayer player, @NotNull Component message) {
        return getChatFormat(get("configuration.universal_chat.format"), player, message);
    }

    public String getUniversalChatPrefix() {
        return get("configuration.universal_chat.prefix");
    }

    public @NotNull BaseComponent[] getGlobalChatFormat(@NotNull ProxiedPlayer player, @NotNull Component message) {
        return getChatFormat(get("configuration.global_chat.format"), player, message);
    }

    public @NotNull BaseComponent[] getGlobalChatSpyFormat(@NotNull ProxiedPlayer player, @NotNull Component message) {
        return getChatFormat(get("configuration.global_chat.spy_format"), player, message);
    }

    public String getGlobalChatPrefix() {
        return get("configuration.global_chat.prefix");
    }

    public @NotNull BaseComponent[] getOtherServerChatSpyFormat(@NotNull ProxiedPlayer player,
                                                                @NotNull Component message) {
        return getChatFormat(get("configuration.other_server_chat.spy_format"), player, message);
    }

    public Duration getDirectMessagesReplyDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getInt(
                "configuration.direct_messages.reply_time_amount_seconds")));
    }

    public List<String> getBlockedWordsList() {
        return get("configuration.blocked_words");
    }

    public String getConsoleName() {
        return get("language.console_name");
    }

    public String getUnknownServer() {
        return get("language.unknown_server_name");
    }

    public @NotNull BaseComponent[] getNoPermission() {
        return TextUtil.toComponentBungee(get("language.no_permission"));
    }

    public @NotNull BaseComponent[] getMessagesTooSmall() {
        return TextUtil.toComponentBungee(get("language.messages_too_small"));
    }

    public @NotNull BaseComponent[] getMessagesTooFrequent() {
        return TextUtil.toComponentBungee(get("language.messages_too_frequent"));
    }

    public @NotNull BaseComponent[] getMessagesTooSimilar() {
        return TextUtil.toComponentBungee(get("language.messages_too_similar"));
    }

    public @NotNull BaseComponent[] getPlayersOnly() {
        return TextUtil.toComponentBungee(get("language.players_only"));
    }

    public @NotNull BaseComponent[] getDirectMessagesHelp() {
        return TextUtil.toComponentBungee(get("language.direct_messages.help"));
    }

    public @NotNull BaseComponent[] getDirectMessagesReplyHelp() {
        return TextUtil.toComponentBungee(get("language.direct_messages.reply_help"));
    }

    public @NotNull BaseComponent[] getDirectMessagesNoPlayerFound() {
        return TextUtil.toComponentBungee(get("language.direct_messages.no_player_found"));
    }

    public @NotNull BaseComponent[] getDirectMessagesOutgoingFormat(@NotNull String receiverName,
                                                                    @NotNull Component message) {
        return TextUtil.toComponentBungee(get("language.direct_messages.outgoing_format"),
                                          Template.of("receiver", receiverName),
                                          Template.of("message", message));
    }

    public @NotNull BaseComponent[] getDirectMessagesIncomingFormat(@NotNull String senderName,
                                                                    @NotNull Component message) {
        return TextUtil.toComponentBungee(get("language.direct_messages.incoming_format"),
                                          Template.of("sender", senderName),
                                          Template.of("message", message));
    }

    public @NotNull BaseComponent[] getDirectMessagesSpyFormat(@NotNull String senderName,
                                                               @NotNull String receiverName,
                                                               @NotNull Component message) {
        return TextUtil.toComponentBungee(get("language.direct_messages.spy_format"),
                                          Template.of("sender", senderName),
                                          Template.of("receiver", receiverName),
                                          Template.of("message", message));
    }

    private @NotNull BaseComponent[] getChatFormat(@Nullable String chatFormat,
                                                   @NotNull ProxiedPlayer player,
                                                   @NotNull Component message) {
        String serverName = Optional.ofNullable(player.getServer())
                                    .map(Server::getInfo)
                                    .map(ServerInfo::getName)
                                    .orElse(getUnknownServer());
        String prefix = TextUtil.getPrefix(player.getUniqueId());
        String suffix = TextUtil.getSuffix(player.getUniqueId());
        String playerName = player.getName();
        Component displayName = TextUtil.toLegacyComponent(prefix + playerName + suffix);
        return TextUtil.toComponentBungee(chatFormat,
                                          Template.of("server", serverName),
                                          Template.of("prefix", prefix),
                                          Template.of("username", playerName),
                                          Template.of("suffix", suffix),
                                          Template.of("displayname", displayName),
                                          Template.of("message", message));
    }
}
