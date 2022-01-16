package com.rafaelsms.potocraft.universalchat.player;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.util.ChatHistory;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class User {

    private final @NotNull UniversalChatPlugin plugin;
    private final @NotNull Player player;
    private final @NotNull UUID playerId;

    private final @NotNull ChatHistory chatHistory;

    private @Nullable UUID replyCandidate = null;
    private @Nullable ZonedDateTime replyCandidateDate = null;

    public User(@NotNull UniversalChatPlugin plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerId = player.getUniqueId();
        this.chatHistory = new PlayerChatHistory();
    }

    public @NotNull ChatHistory getChatHistory() {
        return chatHistory;
    }

    public @NotNull Optional<UUID> getReplyCandidate() {
        ZonedDateTime maxReplyDate =
                ZonedDateTime.now().minus(plugin.getConfiguration().getDirectMessagesReplyDuration());
        if (replyCandidateDate != null && replyCandidateDate.isBefore(maxReplyDate)) {
            return Optional.empty();
        }
        return Optional.ofNullable(replyCandidate);
    }

    public void setReplyCandidate(@Nullable UUID replyCandidate) {
        if (replyCandidate != null) {
            this.replyCandidateDate = ZonedDateTime.now();
        }
        this.replyCandidate = replyCandidate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return playerId.equals(user.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }

    private class PlayerChatHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getConfiguration().getComparatorMinDifferences();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getConfiguration().getComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return plugin.getConfiguration().getLimiterTimeAmount();
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getConfiguration().getLimiterMessageAmount();
        }

        @Override
        protected boolean isPlayerHasBypassPermission() {
            return player.hasPermission(Permissions.CHAT_BYPASS);
        }

    }
}
