package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import com.rafaelsms.discordbot.util.DiscordUtil;
import com.rafaelsms.discordbot.util.PermissionLevel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class TicketListener extends ListenerAdapter {

    private static final String SUGGESTION_ID = "sugestao";
    private static final String REPORT_ID = "denunciar";
    private static final String QUESTION_ID = "pergunta";
    private static final String CHECKOUT_ID = "compras";
    private static final String CLOSE_TICKET_ID = "close-ticket";
    private static final Set<String> OPEN_TICKET_IDS = Set.of(SUGGESTION_ID, REPORT_ID, QUESTION_ID, CHECKOUT_ID);

    private final @NotNull DiscordBot bot;

    public TicketListener(@NotNull DiscordBot bot) {
        this.bot = bot;
        // Prepare guild
        for (Guild guild : bot.getJda().getGuilds()) {
            prepareGuild(guild);
        }
    }

    private void prepareGuild(@NotNull Guild guild) {
        // Prepare ticket lobby category
        Category ticketCategory =
                DiscordUtil.getOrCreateCategory(guild, bot.getConfiguration().getTicketCategoryName());
        // Prepare lobby channel if it doesn't exist
        String lobbyChannelName = bot.getConfiguration().getTicketLobbyChannelName();
        if (!DiscordUtil.doesChannelExists(ticketCategory, lobbyChannelName)) {
            TextChannel lobbyChannel = ticketCategory.createTextChannel(lobbyChannelName).complete();
            // Move to first position
            ticketCategory.modifyTextChannelPositions().selectPosition(0).swapPosition(lobbyChannel).queue();
            // Send open ticket message
            lobbyChannel.sendMessage(bot.getConfiguration().getOpenTicketMessage())
                        .setActionRows(ActionRow.of(Button.of(ButtonStyle.PRIMARY,
                                                              SUGGESTION_ID,
                                                              "Sugerir",
                                                              Emoji.fromUnicode("🤝"))),
                                       ActionRow.of(Button.of(ButtonStyle.PRIMARY,
                                                              REPORT_ID,
                                                              "Denunciar",
                                                              Emoji.fromUnicode("🔪"))),
                                       ActionRow.of(Button.of(ButtonStyle.PRIMARY,
                                                              QUESTION_ID,
                                                              "Pedir ajuda",
                                                              Emoji.fromUnicode("🙋"))),
                                       ActionRow.of(Button.of(ButtonStyle.PRIMARY,
                                                              CHECKOUT_ID,
                                                              "Conseguir compras do site",
                                                              Emoji.fromUnicode("🛍")))).complete();
            // Fix permission for channel
            DiscordUtil.setRolePermissions(lobbyChannel.getPermissionContainer(),
                                           guild.getPublicRole(),
                                           PermissionLevel.VIEW_ONLY);

            // Fix permissions for this category
            DiscordUtil.setRolePermissions(ticketCategory.getPermissionContainer(),
                                           guild.getPublicRole(),
                                           PermissionLevel.NONE);
            bot.getLogger().info("Set up ticket lobby for {}", guild.getName());
        }

        Role operatorRole = DiscordUtil.getOrCreateRole(guild, bot.getConfiguration().getOperatorRoleName());
        DiscordUtil.setRolePermissions(ticketCategory.getPermissionContainer(), operatorRole, PermissionLevel.OPERATOR);
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
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        ButtonInteraction interaction = event.getInteraction();
        String buttonId = interaction.getButton().getId();
        User user = interaction.getUser();
        Member member = interaction.getMember();
        if (buttonId == null || interaction.getGuild() == null || member == null) {
            return;
        }

        // Close ticket interaction
        if (buttonId.equalsIgnoreCase(CLOSE_TICKET_ID)) {
            TextChannel textChannel = event.getInteraction().getTextChannel();
            if (DiscordUtil.isOperator(bot, member)) {
                interaction.reply(bot.getConfiguration().getTicketClosingMessage()).setEphemeral(true).queue();
                interaction.getTextChannel().delete().queue();
                bot.getLogger()
                   .info("Operator {}#{} closed ticket {}",
                         user.getName(),
                         user.getDiscriminator(),
                         textChannel.getName());
                return;
            }

            // Remove user permission otherwise and change name so user can open another one
            DiscordUtil.setMemberPermissions(interaction.getTextChannel().getPermissionContainer(),
                                             member,
                                             PermissionLevel.NONE);
            textChannel.getManager().setName("%s-closed".formatted(textChannel.getName())).queue();
            interaction.reply(bot.getConfiguration().getTicketClosingMessage()).setEphemeral(true).queue();
            bot.getLogger()
               .info("Member {}#{} closed ticket {}", user.getName(), user.getDiscriminator(), textChannel.getName());
            return;
        }

        // Open ticket interactions
        if (!OPEN_TICKET_IDS.contains(buttonId)) {
            return;
        }

        String channelName = "%s-%s-%s".formatted(buttonId.toLowerCase(), user.getName(), user.getDiscriminator());
        List<TextChannel> existingChannels = interaction.getGuild().getTextChannelsByName(channelName, true);
        if (!existingChannels.isEmpty()) {
            String ticketChannelAlreadyExists = bot.getConfiguration()
                                                   .getTicketChannelAlreadyExists()
                                                   .replaceAll("%channel_link%",
                                                               "<#%d>".formatted(existingChannels.get(0).getIdLong()));
            interaction.reply(ticketChannelAlreadyExists).setEphemeral(true).queue();
            return;
        }

        // Create ticket channel and send a redirect message
        Category category =
                DiscordUtil.getOrCreateCategory(interaction.getGuild(), bot.getConfiguration().getTicketCategoryName());
        TextChannel ticketChannel = category.createTextChannel(channelName).complete();

        // Allow no one to see this channel
        DiscordUtil.setRolePermissions(ticketChannel.getPermissionContainer(),
                                       ticketChannel.getGuild().getPublicRole(),
                                       PermissionLevel.NONE);
        // Allow member to interact with the channel
        DiscordUtil.setMemberPermissions(ticketChannel.getPermissionContainer(), member, PermissionLevel.INTERACT);
        // Send a ticket opened message on the channel
        String openedTicketMessage = bot.getConfiguration()
                                        .getOpenedTicketMessage()
                                        .replaceAll("%user_link%", "<@%d>".formatted(member.getIdLong()));
        ticketChannel.sendMessage(openedTicketMessage)
                     .setActionRow(Button.of(ButtonStyle.DANGER, CLOSE_TICKET_ID, "Encerrar ticket",
                                             Emoji.fromUnicode("❌")))
                     .queue();

        // Send "ticket opened" message
        String ticketChannelCreatedMessage = bot.getConfiguration()
                                                .getTicketChannelCreatedMessage()
                                                .replaceAll("%channel_link%",
                                                            "<#%d>".formatted(ticketChannel.getIdLong()));
        interaction.reply(ticketChannelCreatedMessage).setEphemeral(true).queue();
        bot.getLogger().info("Member {}#{} created a ticket for {}", user.getName(), user.getDiscriminator(), buttonId);
    }
}
