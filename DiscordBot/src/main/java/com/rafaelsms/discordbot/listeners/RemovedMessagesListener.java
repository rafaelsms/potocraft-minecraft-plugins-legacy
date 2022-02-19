package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.discordbot.util.DiscordUtil;
import com.rafaelsms.discordbot.util.MessageEntity;
import com.rafaelsms.discordbot.util.PermissionLevel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class RemovedMessagesListener extends ListenerAdapter {

    private final HashMap<String, Message> messageStorage = new HashMap<>();
    private final ArrayDeque<MessageEntity> messageEntities = new ArrayDeque<>();

    private final @NotNull DiscordBot bot;

    public RemovedMessagesListener(@NotNull DiscordBot bot) {
        this.bot = bot;
        for (Guild guild : bot.getJda().getGuilds()) {
            prepareGuild(guild);
        }
    }

    private void prepareGuild(@NotNull Guild guild) {
        List<Role> roleList = guild.getRolesByName(bot.getConfiguration().getOperatorRoleName(), true);
        Role role;
        if (roleList.isEmpty()) {
            role = guild.createRole().setName(bot.getConfiguration().getOperatorRoleName()).complete();
        } else {
            role = roleList.get(0);
        }

        // Create category
        String categoryName = bot.getConfiguration().getRemovedMessagesCategoryName();
        List<Category> categoryList = guild.getCategoriesByName(categoryName, true);
        Category category;
        if (categoryList.isEmpty()) {
            category = guild.createCategory(categoryName).complete();
        } else {
            category = categoryList.get(0);
        }
        // Set permissions
        DiscordUtil.setRolePermissions(category.getPermissionContainer(), role, PermissionLevel.OPERATOR);
        DiscordUtil.setRolePermissions(category.getPermissionContainer(), guild.getPublicRole(), PermissionLevel.NONE);

        // Create text channel
        String channelName = bot.getConfiguration().getRemovedMessagesChannelName();
        TextChannel textChannel = null;
        for (TextChannel channel : category.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                textChannel = channel;
            }
        }
        if (textChannel == null) {
            category.createTextChannel(channelName).complete();
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        prepareGuild(event.getGuild());
    }

    @Override
    public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
        prepareGuild(event.getGuild());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        registerMessage(event.getMessage());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        registerMessage(event.getMessage());
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        Message message = messageStorage.get(event.getMessageId());
        if (message == null || message.getMember() == null) {
            return;
        }
        if (event.getChannel().getName().equalsIgnoreCase(bot.getConfiguration().getRemovedMessagesChannelName())) {
            return;
        }
        Optional<TextChannel> channelOptional = getDeletedMessagesTextChannel(event.getGuild());
        if (channelOptional.isEmpty()) {
            return;
        }
        TextChannel textChannel = channelOptional.get();
        for (Message.Attachment attachment : message.getAttachments()) {
            attachment.downloadToFile().whenComplete((file, throwable) -> {
                if (throwable != null || file == null) {
                    bot.getLogger()
                       .warn("Failed to download attachment {} ({})", attachment.getId(), attachment.getContentType());
                    return;
                }
                textChannel.sendFile(file).queue(msg -> file.delete());
            });
        }
        textChannel.sendMessageEmbeds(DiscordUtil.getQuoteMessage(message.getMember(),
                                                                  Color.ORANGE.getRGB(),
                                                                  message.getContentRaw())).queue();
        User user = message.getMember().getUser();
        bot.getLogger()
           .info("{}#{} deleted a message: {}", user.getName(), user.getDiscriminator(), message.getContentRaw());
    }

    private Optional<TextChannel> getDeletedMessagesTextChannel(Guild guild) {
        List<TextChannel> channelList =
                guild.getTextChannelsByName(bot.getConfiguration().getRemovedMessagesChannelName(), true);
        if (channelList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(channelList.get(0));
    }

    private void registerMessage(@NotNull Message message) {
        messageStorage.put(message.getId(), message);
        messageEntities.add(new MessageEntity(message, ZonedDateTime.now()));
        removeOldEntities();
    }

    private void removeOldEntities() {
        while (!messageEntities.isEmpty()) {
            MessageEntity messageEntity = messageEntities.peekFirst();
            if (messageEntity.dateTime()
                             .plus(bot.getConfiguration().getRemovedMessagesCacheTime())
                             .isBefore(ZonedDateTime.now())) {
                break;
            }
            messageEntities.pop();
        }
    }
}
