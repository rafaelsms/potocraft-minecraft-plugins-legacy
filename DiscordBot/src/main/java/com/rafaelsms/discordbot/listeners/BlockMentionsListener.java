package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.discordbot.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BlockMentionsListener extends ListenerAdapter {

    private final @NotNull DiscordBot bot;

    public BlockMentionsListener(@NotNull DiscordBot bot) {
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
        if (message.getMentionedUsers().isEmpty()) {
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
        DiscordUtil.timeoutMember(bot,
                                  message.getGuild(),
                                  member,
                                  bot.getConfiguration().getMentioningTimeout(),
                                  "mentioning");
        // Check if we should warn users
        if (bot.getConfiguration().isMentionsShouldBeWarned()) {
            message.reply(bot.getConfiguration().getMentionWarningMessage()).queue(warningMessage -> {
                // Delete mention based on configuration
                if (bot.getConfiguration().isMentionsShouldBeRemoved()) {
                    message.delete().queue();
                }
                // Delete warning message based on configuration
                Duration warningDuration = bot.getConfiguration().getMentionsWarningDuration();
                if (warningDuration != null && !warningDuration.isNegative() && warningDuration.toSeconds() > 0) {
                    warningMessage.delete().queueAfter(warningDuration.toMillis(), TimeUnit.MILLISECONDS);
                }
            });
        } else {
            // Delete mention based on configuration
            if (bot.getConfiguration().isMentionsShouldBeRemoved()) {
                message.delete().queue();
            }
        }
    }
}
