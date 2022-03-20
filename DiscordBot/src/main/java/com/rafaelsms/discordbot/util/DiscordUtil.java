package com.rafaelsms.discordbot.util;

import com.rafaelsms.discordbot.DiscordBot;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionContainer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public final class DiscordUtil {

    // Private constructor
    private DiscordUtil() {
    }

    public static boolean doesChannelExists(@NotNull Category category, @NotNull String channelName) {
        for (TextChannel channel : category.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                return true;
            }
        }
        return false;
    }

    public static Category getOrCreateCategory(@NotNull Guild guild, @NotNull String categoryName) {
        List<Category> categories = guild.getCategoriesByName(categoryName, true);
        if (categories.isEmpty()) {
            return guild.createCategory(categoryName).complete();
        }
        return categories.get(0);
    }

    public static Role getOrCreateRole(@NotNull Guild guild, @NotNull String roleName) {
        List<Role> categories = guild.getRolesByName(roleName, true);
        if (categories.isEmpty()) {
            return guild.createRole().setName(roleName).complete();
        }
        return categories.get(0);
    }

    public static boolean isOperator(@NotNull DiscordBot bot, @NotNull Member member) {
        for (Role role : member.getRoles()) {
            if (role.getName().equalsIgnoreCase(bot.getConfiguration().getOperatorRoleName())) {
                return true;
            }
        }
        return false;
    }

    public static void setRolePermissions(@NotNull IPermissionContainer permissionContainer,
                                          @NotNull Role role,
                                          @NotNull PermissionLevel permissionLevel) {
        permissionContainer.getManager()
                           .putRolePermissionOverride(role.getIdLong(),
                                                      permissionLevel.getAllowedPermissions(),
                                                      permissionLevel.getDeniedPermissions())
                           .complete();
    }

    public static void setMemberPermissions(@NotNull IPermissionContainer permissionContainer,
                                            @NotNull Member member,
                                            @NotNull PermissionLevel permissionLevel) {
        permissionContainer.getManager()
                           .putMemberPermissionOverride(member.getIdLong(),
                                                        permissionLevel.getAllowedPermissions(),
                                                        permissionLevel.getDeniedPermissions())
                           .complete();
    }

    public static MessageEmbed getQuoteMessage(Member member, int color, String string) {
        return new MessageEmbed(null,
                                null,
                                string,
                                EmbedType.UNKNOWN,
                                null,
                                color,
                                null,
                                null,
                                new MessageEmbed.AuthorInfo(member.getEffectiveName(),
                                                            member.getEffectiveAvatarUrl(),
                                                            member.getEffectiveAvatarUrl(),
                                                            null),
                                null,
                                null,
                                null,
                                List.of());
    }

    public static void timeoutMember(@NotNull DiscordBot bot,
                                     @NotNull Guild guild,
                                     @NotNull Member member,
                                     @Nullable Duration timeoutDuration,
                                     @NotNull String auditReason) {
        if (timeoutDuration != null && !timeoutDuration.isNegative() && timeoutDuration.toSeconds() > 0) {
            Member botMember = guild.getMember(bot.getJda().getSelfUser());
            if (botMember != null && botMember.canInteract(member)) {
                member.timeoutFor(timeoutDuration).reason(auditReason).queue();
                bot.getLogger().info("Timed out {} for {}", member.getUser().getName(), auditReason);
            }
        }
    }
}
