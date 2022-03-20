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
        if (message.getContentRaw().length() < bot.getConfiguration().getMaximumMessageLength()) {
            return;
        }
        DiscordUtil.timeoutMember(bot,
                                  message.getGuild(),
                                  member,
                                  bot.getConfiguration().getExceededLengthTimeoutDuration(),
                                  "message too long");
        if (bot.getConfiguration().shouldLengthyMessagesBeRemoved()) {
            message.delete().queue();
        }
    }
}
