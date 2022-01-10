package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.user.User;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class PaperUser extends User {

    private final @NotNull Player player;
    private final @NotNull PaperPlugin plugin;

    private final @NotNull GlobalChatHistory globalChatHistory;
    private final @NotNull LocalChatHistory localChatHistory;

    public PaperUser(@NotNull Player player, @NotNull PaperPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.globalChatHistory = new GlobalChatHistory();
        this.localChatHistory = new LocalChatHistory();
    }

    public ChatHistory.ChatResult canSendGlobalMessage(@NotNull String message) {
        return globalChatHistory.canSendMessage(message);
    }

    public void sentGlobalMessage(@NotNull String message) {
        globalChatHistory.sentMessage(message);
    }

    public ChatHistory.ChatResult canSendLocalMessage(@NotNull String message) {
        return localChatHistory.canSendMessage(message);
    }

    public void sentLocalMessage(@NotNull String message) {
        localChatHistory.sentMessage(message);
    }

    private class GlobalChatHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getSettings().getGlobalChatComparatorThreshold();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getSettings().getGlobalChatComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return Duration.ofMillis(plugin.getSettings().getGlobalChatLimiterTimeAmount());
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getGlobalChatLimiterMessageAmount();
        }

        @Override
        public boolean playerHasBypassPermission() {
            return player.hasPermission(Permissions.GLOBAL_CHAT_BYPASS);
        }
    }

    private class LocalChatHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getSettings().getLocalChatComparatorThreshold();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getSettings().getLocalChatComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return Duration.ofMillis(plugin.getSettings().getLocalChatLimiterTimeAmount());
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getLocalChatLimiterMessageAmount();
        }

        @Override
        public boolean playerHasBypassPermission() {
            return player.hasPermission(Permissions.GLOBAL_CHAT_BYPASS);
        }
    }
}
