package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.discordbot.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageLengthLimiter extends ListenerAdapter {

    private final @NotNull DiscordBot bot;

    public MessageLengthLimiter(@NotNull DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        handleMessage(event.getMessage());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        handleMessage(event.getMessage());
    }

    private void handleMessage(@NotNull Message message) {
        if (!bot.getConfiguration().isLengthLimiterEnabled()) {
            return;
        }
        if (message.getCategory() != null &&
            message.getCategory().getName().equalsIgnoreCase(bot.getConfiguration().getTicketCategoryName())) {
            return;
        }
        if (message.getMember() == null) {
            return;
        }
        Member member = message.getMember();
        if (DiscordUtil.isOperator(bot, member) || member.getUser().isBot() || member.getUser().isSystem()) {
            return;
        }
        if (isMessageAppropriate(message.getContentRaw())) {
            return;
        }
        DiscordUtil.timeoutMember(bot,
                                  message.getGuild(),
                                  member,
                                  bot.getConfiguration().getExceededLengthTimeoutDuration(),
                                  "message too long or contains too many new lines or emojis");
        if (bot.getConfiguration().shouldLengthyMessagesBeRemoved()) {
            message.delete().queue();
        }
    }

    private boolean isMessageAppropriate(@NotNull String content) {
        return content.length() <= bot.getConfiguration().getMaximumMessageLength() &&
               countNewLine(content) <= bot.getConfiguration().getMaximumNewLines() &&
               countEmoticons(content) <= bot.getConfiguration().getMaximumEmojiCount();
    }

    private int countNewLine(@NotNull String content) {
        return content.length() - content.replace("\n", "").length();
    }

    private int countEmoticons(@NotNull String content) {
        int emoticons = 0;
        for (char c : content.toCharArray()) {
            if (Character.isSurrogate(c)) {
                emoticons++;
            }
        }
        return emoticons / 2;
    }
}
