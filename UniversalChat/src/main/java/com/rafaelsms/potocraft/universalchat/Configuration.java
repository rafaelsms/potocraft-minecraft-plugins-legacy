package com.rafaelsms.potocraft.universalchat;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

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

    public Component getUniversalChatFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get("configuration.universal_chat.format"), player, message);
    }

    public String getUniversalChatPrefix() {
        return get("configuration.universal_chat.prefix");
    }

    public Component getGlobalChatFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get("configuration.global_chat.format"), player, message);
    }

    public Component getGlobalChatSpyFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get("configuration.global_chat.spy_format"), player, message);
    }

    public String getGlobalChatPrefix() {
        return get("configuration.global_chat.prefix");
    }

    public Component getOtherServerChatSpyFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get("configuration.other_server_chat.spy_format"), player, message);
    }

    public Duration getDirectMessagesReplyDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getInt(
                "configuration.direct_messages.reply_time_amount_seconds")));
    }

    public String getConsoleName() {
        return get("language.console_name");
    }

    public String getUnknownServer() {
        return get("language.unknown_server_name");
    }

    public Component getNoPermission() {
        return TextUtil.toComponent(get("language.no_permission")).build();
    }

    public Component getMessagesTooSmall() {
        return TextUtil.toComponent(get("language.messages_too_small")).build();
    }

    public Component getMessagesTooFrequent() {
        return TextUtil.toComponent(get("language.messages_too_frequent")).build();
    }

    public Component getMessagesTooSimilar() {
        return TextUtil.toComponent(get("language.messages_too_similar")).build();
    }

    public Component getPlayersOnly() {
        return TextUtil.toComponent(get("language.players_only")).build();
    }

    public Component getDirectMessagesHelp() {
        return TextUtil.toComponent(get("language.direct_messages.help")).build();
    }

    public Component getDirectMessagesReplyHelp() {
        return TextUtil.toComponent(get("language.direct_messages.reply_help")).build();
    }

    public Component getDirectMessagesNoPlayerFound() {
        return TextUtil.toComponent(get("language.direct_messages.no_player_found")).build();
    }

    public Component getDirectMessagesOutgoingFormat(@NotNull String receiverName, @NotNull String message) {
        return TextUtil.toComponent(get("language.direct_messages.outgoing_format"))
                       .replace("%receiver%", receiverName)
                       .replace("%message%", message)
                       .build();
    }

    public Component getDirectMessagesIncomingFormat(@NotNull String senderName, @NotNull String message) {
        return TextUtil.toComponent(get("language.direct_messages.incoming_format"))
                       .replace("%sender%", senderName)
                       .replace("%message%", message)
                       .build();
    }

    public Component getDirectMessagesSpyFormat(@NotNull String senderName,
                                                @NotNull String receiverName,
                                                @NotNull String message) {
        return TextUtil.toComponent(get("language.direct_messages.spy_format"))
                       .replace("%sender%", senderName)
                       .replace("%receiver%", receiverName)
                       .replace("%message%", message)
                       .build();
    }

    private Component getChatFormat(@Nullable String chatFormat, @NotNull Player player, @NotNull String message) {
        String serverName = player.getCurrentServer()
                                  .map(ServerConnection::getServer)
                                  .map(RegisteredServer::getServerInfo)
                                  .map(ServerInfo::getName)
                                  .orElse(getUnknownServer());
        return TextUtil.toComponent(chatFormat)
                       .replace("%server%", serverName)
                       .replace("%prefix%", TextUtil.getPrefix(player.getUniqueId()))
                       .replace("%suffix%", TextUtil.getSuffix(player.getUniqueId()))
                       .replace("%username%", player.getUsername())
                       .replace("%message%", message)
                       .build();
    }
}
