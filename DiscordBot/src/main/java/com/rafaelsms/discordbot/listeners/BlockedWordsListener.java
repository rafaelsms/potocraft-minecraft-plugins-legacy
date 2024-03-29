package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.discordbot.util.DiscordUtil;
import com.rafaelsms.potocraft.player.BlockedWordsChecker;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

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
        Member member = message.getMember();
        if (member == null || !bot.getConfiguration().isCursingBlockerEnabled()) {
            return;
        }
        wordsChecker.removeBlockedWords(message.getContentRaw()).ifPresent(string -> {
            MessageAction replacement = message.getTextChannel()
                                               .sendMessageEmbeds(DiscordUtil.getQuoteMessage(member,
                                                                                              Color.ORANGE.getRGB(),
                                                                                              string));
            for (Message.Attachment attachment : message.getAttachments()) {
                attachment.downloadToFile().whenComplete((file, throwable) -> {
                    if (throwable != null || file == null) {
                        bot.getLogger()
                           .warn("Failed to download attachment {} ({})",
                                 attachment.getId(),
                                 attachment.getContentType());
                        return;
                    }
                    message.getTextChannel().sendFile(file).queue(sentMessage -> file.delete());
                });
            }
            // Reply to the same message
            if (message.getMessageReference() != null) {
                replacement.referenceById(message.getMessageReference().getMessageIdLong())
                           .queue(msg -> message.delete().queue());
            } else {
                replacement.queue(msg -> message.delete().queue());
            }
            DiscordUtil.timeoutMember(bot,
                                      message.getGuild(),
                                      member,
                                      bot.getConfiguration().getCursedTimeout(),
                                      "cursing");
        });
    }
}
