package com.rafaelsms.potocraft.universalchat;

import com.rafaelsms.potocraft.util.LuckPermsUtil;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Configuration extends YamlFile {

    public Configuration(@NotNull Path dataDirectory) throws IOException {
        super(dataDirectory.toFile(), "config.yml");
    }

    public Integer getLimiterMessageAmount() {
        return getIntOrNull("configuration.chat_limiter.message_amount");
    }

    public Duration getLimiterTimeAmount() {
        return Duration.ofMillis(Objects.requireNonNull(getLongOrNull("configuration.chat_limiter.time_amount_millis")));
    }

    public Integer getLimiterMinMessageLength() {
        return getIntOrNull("configuration.chat_limiter.min_message_length");
    }

    public Integer getComparatorMinLength() {
        return getIntOrNull("configuration.comparator.min_length_to_compare");
    }

    public Integer getComparatorMinDifferences() {
        return getIntOrNull("configuration.comparator.min_char_differences");
    }

    public @NotNull BaseComponent[] getUniversalChatFormat(@NotNull ProxiedPlayer player, @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.universal_chat.format"), player, message);
    }

    public String getUniversalChatPrefix() {
        return getOrThrow("configuration.universal_chat.prefix");
    }

    public @NotNull BaseComponent[] getGlobalChatFormat(@NotNull ProxiedPlayer player, @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.global_chat.format"), player, message);
    }

    public @NotNull BaseComponent[] getGlobalChatSpyFormat(@NotNull ProxiedPlayer player, @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.global_chat.spy_format"), player, message);
    }

    public String getGlobalChatPrefix() {
        return getOrThrow("configuration.global_chat.prefix");
    }

    public @NotNull BaseComponent[] getOtherServerChatSpyFormat(@NotNull ProxiedPlayer player,
                                                                @NotNull Component message) {
        return getChatFormat(getOrThrow("configuration.other_server_chat.spy_format"), player, message);
    }

    public Duration getDirectMessagesReplyDuration() {
        return Duration.ofSeconds(Objects.requireNonNull(getIntOrNull(
                "configuration.direct_messages.reply_time_amount_seconds")));
    }

    public boolean getBroadcastEnabled() {
        return Objects.requireNonNull(getOrThrow("configuration.broadcast_messages.enabled"));
    }

    public Duration getBroadcastPeriod() {
        return Duration.ofSeconds(Objects.requireNonNull(getIntOrNull(
                "configuration.broadcast_messages.period_in_seconds")));
    }

    @SuppressWarnings("unchecked")
    public List<BaseComponent[]> getBroadcastMessages() {
        return Util.convertList((Collection<String>) getOrThrow("configuration.broadcast_messages.messages"),
                                TextUtil::toComponentBungee);
    }

    public List<String> getBlockedWordsList() {
        return getOrThrow("configuration.blocked_words");
    }

    public String getConsoleName() {
        return getOrThrow("language.console_name");
    }

    public String getUnknownServer() {
        return getOrThrow("language.unknown_server_name");
    }

    public @NotNull BaseComponent[] getNoPermission() {
        return TextUtil.toComponentBungee(getOrThrow("language.no_permission"));
    }

    public @NotNull BaseComponent[] getMessagesTooSmall() {
        return TextUtil.toComponentBungee(getOrThrow("language.messages_too_small"));
    }

    public @NotNull BaseComponent[] getMessagesTooFrequent() {
        return TextUtil.toComponentBungee(getOrThrow("language.messages_too_frequent"));
    }

    public @NotNull BaseComponent[] getMessagesTooSimilar() {
        return TextUtil.toComponentBungee(getOrThrow("language.messages_too_similar"));
    }

    public @NotNull BaseComponent[] getPlayersOnly() {
        return TextUtil.toComponentBungee(getOrThrow("language.players_only"));
    }

    public @NotNull BaseComponent[] getDirectMessagesHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.direct_messages.help"));
    }

    public @NotNull BaseComponent[] getDirectMessagesReplyHelp() {
        return TextUtil.toComponentBungee(getOrThrow("language.direct_messages.reply_help"));
    }

    public @NotNull BaseComponent[] getDirectMessagesNoPlayerFound() {
        return TextUtil.toComponentBungee(getOrThrow("language.direct_messages.no_player_found"));
    }

    public @NotNull BaseComponent[] getDirectMessagesOutgoingFormat(@NotNull String receiverName,
                                                                    @NotNull Component message) {
        return TextUtil.toComponentBungee(getOrThrow("language.direct_messages.outgoing_format"),
                                          Placeholder.parsed("receiver", receiverName),
                                          Placeholder.component("message", message));
    }

    public @NotNull BaseComponent[] getDirectMessagesIncomingFormat(@NotNull String senderName,
                                                                    @NotNull Component message) {
        return TextUtil.toComponentBungee(getOrThrow("language.direct_messages.incoming_format"),
                                          Placeholder.parsed("sender", senderName),
                                          Placeholder.component("message", message));
    }

    public @NotNull BaseComponent[] getDirectMessagesSpyFormat(@NotNull String senderName,
                                                               @NotNull String receiverName,
                                                               @NotNull Component message) {
        return TextUtil.toComponentBungee(getOrThrow("language.direct_messages.spy_format"),
                                          Placeholder.parsed("sender", senderName),
                                          Placeholder.parsed("receiver", receiverName),
                                          Placeholder.component("message", message));
    }

    private @NotNull BaseComponent[] getChatFormat(@Nullable String chatFormat,
                                                   @NotNull ProxiedPlayer player,
                                                   @NotNull Component message) {
        String serverName = Optional.ofNullable(player.getServer())
                                    .map(Server::getInfo)
                                    .map(ServerInfo::getName)
                                    .orElse(getUnknownServer());
        String prefix = LuckPermsUtil.getUncoloredPrefix(player.getUniqueId()).orElse("");
        String suffix = LuckPermsUtil.getUncoloredSuffix(player.getUniqueId()).orElse("");
        String playerName = player.getName();
        Component displayName = TextUtil.toLegacyComponent(prefix + playerName + suffix);
        return TextUtil.toComponentBungee(chatFormat,
                                          Placeholder.parsed("server", serverName),
                                          Placeholder.parsed("prefix", prefix),
                                          Placeholder.parsed("username", playerName),
                                          Placeholder.parsed("suffix", suffix),
                                          Placeholder.component("displayname", displayName),
                                          Placeholder.component("message", message));
    }
}
