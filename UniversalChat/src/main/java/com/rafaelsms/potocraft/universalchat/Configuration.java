package com.rafaelsms.potocraft.universalchat;

import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    public Configuration(@NotNull Path dataDirectory) throws IOException {
        super(getConfigurationFile(dataDirectory));
        loadConfiguration();
    }

    private static @NotNull File getConfigurationFile(Path dataDirectory) throws IOException {
        File directory = dataDirectory.toFile();
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Couldn't create data folder");
        }
        return new File(directory, "config.yml");
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(Keys.LIMITER_MESSAGE_AMOUNT, 4);
        defaults.put(Keys.LIMITER_TIME_AMOUNT_MILLIS, 12000);
        defaults.put(Keys.LIMITER_MIN_LENGTH, 3);
        defaults.put(Keys.COMPARATOR_MIN_LENGTH, 4);
        defaults.put(Keys.COMPARATOR_MIN_DIFFERENCES, 3);

        defaults.put(Keys.UNIVERSAL_CHAT_FORMAT, "&6!! <&e%prefix%%username%%suffix%&6> &f%message%");
        defaults.put(Keys.UNIVERSAL_CHAT_PREFIX, "!!");
        defaults.put(Keys.GLOBAL_CHAT_FORMAT, "&3! <&e%prefix%%username%%suffix%&3> &f%message%");
        defaults.put(Keys.GLOBAL_CHAT_SPY_FORMAT, "&7! <&e%prefix%%username%%suffix%&7> &f%message%");
        defaults.put(Keys.GLOBAL_CHAT_PREFIX, "!");
        defaults.put(Keys.DIRECT_MESSAGES_REPLY_SECONDS, 60 * 4);
        defaults.put(Keys.OTHER_SERVER_CHAT_SPY_FORMAT, "&7%prefix%%username%%suffix &7(outro server) &7%message%");

        defaults.put(Keys.LANG_CONSOLE_NAME, "administração");
        defaults.put(Keys.LANG_NO_PERMISSION, "&cVocê não possui permissão.");
        defaults.put(Keys.LANG_MESSAGES_TOO_SMALL, "&cMensagens muito pequenas são ignoradas!");
        defaults.put(Keys.LANG_MESSAGES_TOO_FREQUENT, "&cMensagens enviadas muito rapidamente!");
        defaults.put(Keys.LANG_MESSAGES_TOO_SIMILAR, "&cMensagens muito parecidas!");
        defaults.put(Keys.LANG_PLAYERS_ONLY, "&cComando disponível apenas para jogadores!");

        defaults.put(Keys.LANG_DIRECT_MESSAGES_HELP, "&6Digite &e&l/mensagem <nome> <mensagem>");
        defaults.put(Keys.LANG_DIRECT_MESSAGES_REPLY_HELP,
                     "&6Digite &e&l/responder <mensagem>&6, a mensagem irá para a última pessoa com quem falou.");
        defaults.put(Keys.LANG_DIRECT_MESSAGES_NO_PLAYER_FOUND, "&cJogador não encontrado!");
        defaults.put(Keys.LANG_DIRECT_MESSAGES_OUTGOING_FORMAT, "&3você &7-> &3%receiver% &7%message%");
        defaults.put(Keys.LANG_DIRECT_MESSAGES_INCOMING_FORMAT, "&3%sender% &7-> &3você &7%message%");
        defaults.put(Keys.LANG_DIRECT_MESSAGES_SPY_FORMAT, "&3%sender% &7-> &3%receiver% &7%message%");

        return defaults;
    }

    public int getLimiterMessageAmount() {
        return get(Keys.LIMITER_MESSAGE_AMOUNT);
    }

    public Duration getLimiterTimeAmount() {
        Integer millis = get(Keys.LIMITER_TIME_AMOUNT_MILLIS);
        return Duration.ofMillis(millis.longValue());
    }

    public int getLimiterMinMessageLength() {
        return get(Keys.LIMITER_MIN_LENGTH);
    }

    public int getComparatorMinLength() {
        return get(Keys.COMPARATOR_MIN_LENGTH);
    }

    public int getComparatorMinDifferences() {
        return get(Keys.COMPARATOR_MIN_DIFFERENCES);
    }

    public Component getUniversalChatFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get(Keys.UNIVERSAL_CHAT_FORMAT), player, message);
    }

    public String getUniversalChatPrefix() {
        return get(Keys.UNIVERSAL_CHAT_PREFIX);
    }

    public Component getGlobalChatFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get(Keys.GLOBAL_CHAT_FORMAT), player, message);
    }

    public Component getGlobalChatSpyFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get(Keys.GLOBAL_CHAT_SPY_FORMAT), player, message);
    }

    public String getGlobalChatPrefix() {
        return get(Keys.GLOBAL_CHAT_PREFIX);
    }

    public Component getOtherServerChatSpyFormat(@NotNull Player player, @NotNull String message) {
        return getChatFormat(get(Keys.OTHER_SERVER_CHAT_SPY_FORMAT), player, message);
    }

    public Duration getDirectMessagesReplyDuration() {
        Integer seconds = get(Keys.DIRECT_MESSAGES_REPLY_SECONDS);
        return Duration.ofSeconds(seconds.longValue());
    }

    public String getConsoleName() {
        return get(Keys.LANG_CONSOLE_NAME);
    }

    public Component getNoPermission() {
        return TextUtil.toComponent(get(Keys.LANG_NO_PERMISSION));
    }

    public Component getMessagesTooSmall() {
        return TextUtil.toComponent(get(Keys.LANG_MESSAGES_TOO_SMALL));
    }

    public Component getMessagesTooFrequent() {
        return TextUtil.toComponent(get(Keys.LANG_MESSAGES_TOO_FREQUENT));
    }

    public Component getMessagesTooSimilar() {
        return TextUtil.toComponent(get(Keys.LANG_MESSAGES_TOO_SIMILAR));
    }

    public Component getPlayersOnly() {
        return TextUtil.toComponent(get(Keys.LANG_PLAYERS_ONLY));
    }

    public Component getDirectMessagesHelp() {
        return TextUtil.toComponent(get(Keys.LANG_DIRECT_MESSAGES_HELP));
    }

    public Component getDirectMessagesReplyHelp() {
        return TextUtil.toComponent(get(Keys.LANG_DIRECT_MESSAGES_REPLY_HELP));
    }

    public Component getDirectMessagesNoPlayerFound() {
        return TextUtil.toComponent(get(Keys.LANG_DIRECT_MESSAGES_NO_PLAYER_FOUND));
    }

    public Component getDirectMessagesOutgoingFormat(@NotNull String receiverName, @NotNull String message) {
        return TextUtil
                .toComponent(get(Keys.LANG_DIRECT_MESSAGES_OUTGOING_FORMAT))
                .replaceText(TextUtil.replaceText("%receiver%", receiverName))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    public Component getDirectMessagesIncomingFormat(@NotNull String senderName, @NotNull String message) {
        return TextUtil
                .toComponent(get(Keys.LANG_DIRECT_MESSAGES_INCOMING_FORMAT))
                .replaceText(TextUtil.replaceText("%sender%", senderName))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    public Component getDirectMessagesSpyFormat(@NotNull String senderName,
                                                @NotNull String receiverName,
                                                @NotNull String message) {
        return TextUtil
                .toComponent(get(Keys.LANG_DIRECT_MESSAGES_SPY_FORMAT))
                .replaceText(TextUtil.replaceText("%sender%", senderName))
                .replaceText(TextUtil.replaceText("%receiver%", receiverName))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    private Component getChatFormat(@NotNull String chatFormat, @NotNull Player player, @NotNull String message) {
        return TextUtil
                .toComponent(chatFormat)
                .replaceText(TextUtil.replaceText("%prefix%", TextUtil.getPrefix(player.getUniqueId())))
                .replaceText(TextUtil.replaceText("%suffix%", TextUtil.getSuffix(player.getUniqueId())))
                .replaceText(TextUtil.replaceText("%username%", player.getUsername()))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    private static final class Keys {

        public static final String LIMITER_MESSAGE_AMOUNT = "configuration.chat_limiter.message_amount";
        public static final String LIMITER_TIME_AMOUNT_MILLIS = "configuration.chat_limiter.time_amount_millis";
        public static final String LIMITER_MIN_LENGTH = "configuration.chat_limiter.min_message_length";
        public static final String COMPARATOR_MIN_LENGTH = "configuration.comparator.min_length_to_compare";
        public static final String COMPARATOR_MIN_DIFFERENCES = "configuration.comparator.min_char_differences";

        public static final String UNIVERSAL_CHAT_FORMAT = "configuration.universal_chat.format";
        public static final String UNIVERSAL_CHAT_PREFIX = "configuration.universal_chat.prefix";
        public static final String GLOBAL_CHAT_FORMAT = "configuration.global_chat.format";
        public static final String GLOBAL_CHAT_SPY_FORMAT = "configuration.global_chat.spy_format";
        public static final String GLOBAL_CHAT_PREFIX = "configuration.global_chat.prefix";
        public static final String DIRECT_MESSAGES_REPLY_SECONDS =
                "configuration.direct_messages.reply_time_amount_seconds";
        public static final String OTHER_SERVER_CHAT_SPY_FORMAT = "configuration.other_server_chat.spy_format";

        public static final String LANG_CONSOLE_NAME = "language.console_name";
        public static final String LANG_NO_PERMISSION = "language.no_permission";
        public static final String LANG_MESSAGES_TOO_SMALL = "language.messages_too_small";
        public static final String LANG_MESSAGES_TOO_FREQUENT = "language.messages_too_frequent";
        public static final String LANG_MESSAGES_TOO_SIMILAR = "language.messages_too_similar";
        public static final String LANG_PLAYERS_ONLY = "language.players_only";

        public static final String LANG_DIRECT_MESSAGES_HELP = "language.direct_messages.help";
        public static final String LANG_DIRECT_MESSAGES_REPLY_HELP = "language.direct_messages.reply_help";
        public static final String LANG_DIRECT_MESSAGES_NO_PLAYER_FOUND = "language.direct_messages.no_player_found";
        public static final String LANG_DIRECT_MESSAGES_OUTGOING_FORMAT = "language.direct_messages.outgoing_format";
        public static final String LANG_DIRECT_MESSAGES_INCOMING_FORMAT = "language.direct_messages.incoming_format";
        public static final String LANG_DIRECT_MESSAGES_SPY_FORMAT = "language.direct_messages.spy_format";

        // Private constructor
        private Keys() {
        }
    }
}
