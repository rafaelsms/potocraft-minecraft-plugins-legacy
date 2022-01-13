package com.rafaelsms.potocraft.velocity.user;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.user.User;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class VelocityUser extends User {

    private final @NotNull VelocityPlugin plugin;
    private final @NotNull Player player;

    private final @NotNull UniversalMessageHistory universalMessageHistory;
    private final @NotNull DirectMessageHistory directMessageHistory;

    private @Nullable UUID lastReplyCandidate = null;
    private @Nullable ZonedDateTime lastPrivateMessageDate = null;

    public VelocityUser(@NotNull VelocityPlugin plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.universalMessageHistory = new UniversalMessageHistory();
        this.directMessageHistory = new DirectMessageHistory();
    }

    public ChatHistory.ChatResult canSendUniversalMessage(@NotNull String message) {
        return universalMessageHistory.canSendMessage(message);
    }

    public void sentUniversalMessage(@NotNull String message) {
        universalMessageHistory.sentMessage(message);
    }

    public ChatHistory.ChatResult canSendDirectMessages(@NotNull String message) {
        return directMessageHistory.canSendMessage(message);
    }

    public void sentDirectMessage(@NotNull String message) {
        directMessageHistory.sentMessage(message);
    }

    public Optional<UUID> getLastReplyCandidate() {
        ZonedDateTime expirationDate = ZonedDateTime.now().minus(plugin.getSettings().getPrivateMessageTimeout());
        if (lastPrivateMessageDate == null || lastPrivateMessageDate.isBefore(expirationDate)) {
            return Optional.empty();
        }
        return Optional.ofNullable(lastReplyCandidate);
    }

    public void setLastReplyCandidate(@Nullable UUID lastReplyCandidate) {
        if (lastReplyCandidate != null) {
            lastPrivateMessageDate = ZonedDateTime.now();
        }
        this.lastReplyCandidate = lastReplyCandidate;
    }

    private class DirectMessageHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getSettings().getDirectMessageComparatorThreshold();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getSettings().getDirectMessageComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return plugin.getSettings().getDirectMessageLimiterTimeAmount();
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getDirectMessageLimiterMessageAmount();
        }

        @Override
        public boolean playerHasBypassPermission() {
            return player.hasPermission(Permissions.MESSAGE_COMMAND_BYPASS_LIMITER);
        }
    }

    private class UniversalMessageHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getSettings().getUniversalChatComparatorThreshold();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getSettings().getUniversalChatComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return plugin.getSettings().getUniversalChatLimiterTimeAmount();
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getUniversalChatLimiterMessageAmount();
        }

        @Override
        public boolean playerHasBypassPermission() {
            return player.hasPermission(Permissions.UNIVERSAL_CHAT_BYPASS);
        }
    }
}
