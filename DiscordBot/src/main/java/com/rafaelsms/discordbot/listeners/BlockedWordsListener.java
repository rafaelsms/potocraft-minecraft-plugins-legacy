package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.potocraft.util.BlockedWordsChecker;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BlockedWordsListener extends ListenerAdapter {

    private final @NotNull DiscordBot bot;
    private final @NotNull BlockedWordsChecker wordsChecker;

    public BlockedWordsListener(@NotNull DiscordBot bot) {
        this.bot = bot;
        this.wordsChecker = new BlockedWordsChecker(bot.getLogger(), bot.getConfiguration().getBlockedWords());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        handleMessageContent(event.getMessage());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        handleMessageContent(event.getMessage());
    }

    private void handleMessageContent(Message message) {
        wordsChecker.removeBlockedWords(message.getContentRaw()).ifPresent(string -> {
            message.editMessage(string).queue();
            Member botMember = message.getGuild().getMember(bot.getJda().getSelfUser());
            if (message.getMember() != null && botMember != null && botMember.canInteract(message.getMember())) {
                message.getMember().timeoutFor(bot.getConfiguration().getCursedTimeout()).reason("cursing").queue();
                bot.getLogger().info("Timed out {} for cursing", message.getMember().getUser().getName());
            }
        });
    }
}
