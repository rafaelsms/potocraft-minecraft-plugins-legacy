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
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatController {

    private final @NotNull VelocityPlugin plugin;

    public ChatController(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.EARLY)
    private void checkUniversalChatConditions(PlayerChatEvent event) {
        // Skip not allowed messages
        if (!event.getResult().isAllowed()) {
            return;
        }

        Player sendingPlayer = event.getPlayer();
        // Ignore if not universal chat
        String chatPrefix = plugin.getSettings().getUniversalChatPrefix();
        if (!event.getMessage().startsWith(chatPrefix)) {
            return;
        }

        // Check if player has permission
        if (!sendingPlayer.hasPermission(Permissions.UNIVERSAL_CHAT)) {
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
        ChatHistory.ChatResult chatResult = user.canSendUniversalMessage(message);
        if (!chatResult.isAllowed()) {
            if (chatResult == ChatHistory.ChatResult.SIMILAR_MESSAGES) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooSimilar());
            } else if (chatResult == ChatHistory.ChatResult.TOO_FREQUENT) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooFrequent());
            }
            event.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }

    @Subscribe(order = PostOrder.LAST)
    private void sendUniversalChatSpy(PlayerChatEvent event) {
        // Skip not allowed messages
        if (!event.getResult().isAllowed()) {
            return;
        }

        // All messages are sent to spies (don't filter for prefix)
        Player sendingPlayer = event.getPlayer();

        // Format spy message
        Component spyChatMessage = Util.applyChatFormat(
                plugin.getSettings().getUniversalChatSpyFormat(),
                sendingPlayer.getUniqueId(),
                sendingPlayer.getUsername(),
                Component.text(event.getMessage())
        );
        // Send to all spies
        for (Player onlinePlayer : plugin.getProxyServer().getAllPlayers()) {
            // If player doesn't have permission, skip
            if (!onlinePlayer.hasPermission(Permissions.UNIVERSAL_CHAT_SPY)) {
                continue;
            }
            // Skip if on same server
            if (onlinePlayer.getCurrentServer().isPresent() && sendingPlayer.getCurrentServer().isPresent()) {
                ServerInfo onlinePlayerServerInfo = onlinePlayer.getCurrentServer().get().getServerInfo();
                ServerInfo sendingServerInfo = sendingPlayer.getCurrentServer().get().getServerInfo();
                if (onlinePlayerServerInfo.getName().equalsIgnoreCase(sendingServerInfo.getName())) {
                    continue;
                }
            }
            // Send spy message
            onlinePlayer.sendMessage(sendingPlayer.identity(), spyChatMessage, MessageType.CHAT);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    private void sendUniversalChat(PlayerChatEvent event) {
        // Skip not allowed messages
        if (!event.getResult().isAllowed()) {
            return;
        }

        Player sendingPlayer = event.getPlayer();
        // Ignore if not universal chat
        String chatPrefix = plugin.getSettings().getUniversalChatPrefix();
        if (!event.getMessage().startsWith(chatPrefix)) {
            return;
        }

        // Format chat
        String message = event.getMessage().substring(chatPrefix.length()).strip();
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
        plugin.getUserManager().getUser(sendingPlayer.getUniqueId()).sentUniversalMessage(message);
    }
}
