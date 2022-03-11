package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.discordbot.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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
        message.getTextChannel();
        DiscordUtil.timeoutMember(bot,
                                  message.getGuild(),
                                  member,
                                  bot.getConfiguration().getMentioningTimeout(),
                                  "mentioning");
        message.reply(bot.getConfiguration().getMentionWarningMessage()).queue(warningMessage -> {
            if (bot.getConfiguration().isMentionsShouldBeRemoved()) {
                message.delete().queue();
            }
        });
    }
}
