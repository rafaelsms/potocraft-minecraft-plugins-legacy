package com.rafaelsms.potocraft.velocity.user;

import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.user.User;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class VelocityUser extends User {

    private final @NotNull VelocityPlugin plugin;
    private final @NotNull Player player;

    private final @NotNull UniversalMessageHistory universalMessageHistory;

    public VelocityUser(@NotNull VelocityPlugin plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.universalMessageHistory = new UniversalMessageHistory();
    }

    public ChatHistory.ChatResult canSendMessage(@NotNull String message) {
        return universalMessageHistory.canSendMessage(message);
    }

    public void sentUniversalMessage(@NotNull String message) {
        universalMessageHistory.sentMessage(message);
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
        protected Duration getLimiterTimeFrame() {
            return Duration.ofMillis(plugin.getSettings().getUniversalChatLimiterTimeAmount());
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getUniversalChatLimiterMessageAmount();
        }
    }
}
