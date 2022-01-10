package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatController {

    private final @NotNull VelocityPlugin plugin;

    public ChatController(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LATE)
    private void onUniversalChat(PlayerChatEvent event) {
        if (!event.getResult().isAllowed()) return;

        Player sendingPlayer = event.getPlayer();
        // Ignore if not universal chat
        String chatPrefix = plugin.getSettings().getUniversalChatPrefix();
        if (!event.getMessage().startsWith(chatPrefix)) {
            return;
        }

        // Check if player has permission
        if (!event.getPlayer().hasPermission(Permissions.UNIVERSAL_CHAT)) {
            sendingPlayer.sendMessage(plugin.getSettings().getNoPermission());
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        // Format message
        String message = event.getMessage().substring(chatPrefix.length()).strip();
        if (message.isEmpty()) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        // Apply chat comparator
        VelocityUser user = plugin.getUserManager().getUser(sendingPlayer.getUniqueId());
        ChatHistory.ChatResult chatResult = user.canSendMessage(message);
        if (!chatResult.isAllowed()) {
            if (chatResult == ChatHistory.ChatResult.SIMILAR_MESSAGES) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooSimilar());
            } else if (chatResult == ChatHistory.ChatResult.TOO_FREQUENT) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooFrequent());
            }
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        // Format chat
        Component chatMessage = Util.applyChatFormat(
                plugin.getSettings().getUniversalChatFormat(),
                sendingPlayer.getUniqueId(),
                sendingPlayer.getUsername(),
                Component.text(message)
        );
        // Cancel the event
        event.setResult(PlayerChatEvent.ChatResult.denied());
        // Send the message to everybody
        plugin.getProxyServer().sendMessage(sendingPlayer.identity(), chatMessage, MessageType.CHAT);
        // Store the message on the user's message history
        user.sentUniversalMessage(message);
    }
}
