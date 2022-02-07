package com.rafaelsms.potocraft.universalchat;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
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

    public @NotNull BaseComponent[] getUniversalChatFormat(@NotNull ProxiedPlayer player, @NotNull String message) {
        return getChatFormat(get("configuration.universal_chat.format"), player, message);
    }

    public String getUniversalChatPrefix() {
        return get("configuration.universal_chat.prefix");
    }

    public @NotNull BaseComponent[] getGlobalChatFormat(@NotNull ProxiedPlayer player, @NotNull String message) {
        return getChatFormat(get("configuration.global_chat.format"), player, message);
    }

    public @NotNull BaseComponent[] getGlobalChatSpyFormat(@NotNull ProxiedPlayer player, @NotNull String message) {
        return getChatFormat(get("configuration.global_chat.spy_format"), player, message);
    }

    public String getGlobalChatPrefix() {
        return get("configuration.global_chat.prefix");
    }

    public @NotNull BaseComponent[] getOtherServerChatSpyFormat(@NotNull ProxiedPlayer player,
                                                                @NotNull String message) {
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

    public @NotNull BaseComponent[] getNoPermission() {
        return TextUtil.toComponent(get("language.no_permission")).buildBungee();
    }

    public @NotNull BaseComponent[] getMessagesTooSmall() {
        return TextUtil.toComponent(get("language.messages_too_small")).buildBungee();
    }

    public @NotNull BaseComponent[] getMessagesTooFrequent() {
        return TextUtil.toComponent(get("language.messages_too_frequent")).buildBungee();
    }

    public @NotNull BaseComponent[] getMessagesTooSimilar() {
        return TextUtil.toComponent(get("language.messages_too_similar")).buildBungee();
    }

    public @NotNull BaseComponent[] getPlayersOnly() {
        return TextUtil.toComponent(get("language.players_only")).buildBungee();
    }

    public @NotNull BaseComponent[] getDirectMessagesHelp() {
        return TextUtil.toComponent(get("language.direct_messages.help")).buildBungee();
    }

    public @NotNull BaseComponent[] getDirectMessagesReplyHelp() {
        return TextUtil.toComponent(get("language.direct_messages.reply_help")).buildBungee();
    }

    public @NotNull BaseComponent[] getDirectMessagesNoPlayerFound() {
        return TextUtil.toComponent(get("language.direct_messages.no_player_found")).buildBungee();
    }

    public @NotNull BaseComponent[] getDirectMessagesOutgoingFormat(@NotNull String receiverName,
                                                                    @NotNull String message) {
        return TextUtil.toComponent(get("language.direct_messages.outgoing_format"))
                       .replace("%receiver%", receiverName)
                       .replace("%message%", message)
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getDirectMessagesIncomingFormat(@NotNull String senderName,
                                                                    @NotNull String message) {
        return TextUtil.toComponent(get("language.direct_messages.incoming_format"))
                       .replace("%sender%", senderName)
                       .replace("%message%", message)
                       .buildBungee();
    }

    public @NotNull BaseComponent[] getDirectMessagesSpyFormat(@NotNull String senderName,
                                                               @NotNull String receiverName,
                                                               @NotNull String message) {
        return TextUtil.toComponent(get("language.direct_messages.spy_format"))
                       .replace("%sender%", senderName)
                       .replace("%receiver%", receiverName)
                       .replace("%message%", message)
                       .buildBungee();
    }

    private @NotNull BaseComponent[] getChatFormat(@Nullable String chatFormat,
                                                   @NotNull ProxiedPlayer player,
                                                   @NotNull String message) {
        String serverName = Optional.ofNullable(player.getServer())
                                    .map(Server::getInfo)
                                    .map(ServerInfo::getName)
                                    .orElse(getUnknownServer());
        return TextUtil.toComponent(chatFormat)
                       .replace("%server%", serverName)
                       .replace("%prefix%", TextUtil.getPrefix(player.getUniqueId()))
                       .replace("%suffix%", TextUtil.getSuffix(player.getUniqueId()))
                       .replace("%username%", player.getName())
                       .replace("%message%", message)
                       .buildBungee();
    }
}
