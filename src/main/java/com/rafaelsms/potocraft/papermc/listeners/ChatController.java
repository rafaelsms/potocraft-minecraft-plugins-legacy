package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ChatController implements Listener {

    private final @NotNull PaperPlugin plugin;

    public ChatController(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    private void handleChatResult(AsyncChatEvent event, Player sendingPlayer, ChatHistory.ChatResult chatResult) {
        if (!chatResult.isAllowed()) {
            if (chatResult == ChatHistory.ChatResult.SIMILAR_MESSAGES) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooSimilar());
            } else if (chatResult == ChatHistory.ChatResult.TOO_FREQUENT) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooFrequent());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void checkLocalChatConditions(AsyncChatEvent event) {
        Player sendingPlayer = event.getPlayer();

        // Ignore when prefixed with global prefix
        String uncoloredString = TextUtil.toSimpleString(event.message());
        if (uncoloredString.startsWith(plugin.getSettings().getGlobalChatPrefix())) {
            return;
        }

        // Check if message have any content
        String messageString = uncoloredString.strip();
        if (messageString.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        // Check chat history and frequency
        PaperUser user = plugin.getUserManager().getUser(sendingPlayer.getUniqueId());
        ChatHistory.ChatResult chatResult = user.canSendLocalMessage(messageString);
        handleChatResult(event, sendingPlayer, chatResult);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void sendLocalChat(AsyncChatEvent event) {
        Player sendingPlayer = event.getPlayer();

        // Ignore when prefixed with global prefix
        String uncoloredString = TextUtil.toSimpleString(event.message());
        if (uncoloredString.startsWith(plugin.getSettings().getGlobalChatPrefix())) {
            return;
        }

        // Format chat
        Component chatMessage = TextUtil.applyChatFormat(plugin.getSettings().getLocalChatFormat(),
                                                         sendingPlayer.getUniqueId(),
                                                         sendingPlayer.getName(),
                                                         event.message());
        Component spyChatMessage = TextUtil.applyChatFormat(plugin.getSettings().getLocalChatSpyFormat(),
                                                            sendingPlayer.getUniqueId(),
                                                            sendingPlayer.getName(),
                                                            event.message());
        // Cancel the event
        event.setCancelled(true);

        // Store the message on the user's message history
        plugin.getUserManager().getUser(sendingPlayer.getUniqueId()).sentLocalMessage(uncoloredString.strip());
        // Send the message to people nearby
        double chatRadius = plugin.getSettings().getLocalChatRadius();
        Location sendingLocation = sendingPlayer.getLocation();
        if (chatRadius > 0) {
            double chatRadiusSq = chatRadius * chatRadius;
            UUID worldId = sendingLocation.getWorld().getUID();
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                Location playerLocation = onlinePlayer.getLocation();
                if (!Objects.equals(playerLocation.getWorld().getUID(), worldId)) {
                    // Send message if player has spy permission
                    if (onlinePlayer.hasPermission(Permissions.LOCAL_CHAT_SPY)) {
                        onlinePlayer.sendMessage(sendingPlayer.identity(), spyChatMessage, MessageType.CHAT);
                    }
                    continue;
                }
                // If far away, don't send message except if it has spy permissions
                if (playerLocation.distanceSquared(sendingLocation) > chatRadiusSq) {
                    // Send message if player has spy permission
                    if (onlinePlayer.hasPermission(Permissions.LOCAL_CHAT_SPY)) {
                        onlinePlayer.sendMessage(sendingPlayer.identity(), spyChatMessage, MessageType.CHAT);
                    }
                    continue;
                }
                onlinePlayer.sendMessage(sendingPlayer.identity(), chatMessage, MessageType.CHAT);
            }
        } else if (chatRadius == 0) {
            // Send message to entire world only
            for (Player player : sendingLocation.getWorld().getPlayers()) {
                player.sendMessage(sendingPlayer.identity(), chatMessage, MessageType.CHAT);
            }
        } else {
            // Send to entire server if negative
            plugin.getServer().sendMessage(sendingPlayer.identity(), chatMessage, MessageType.CHAT);
            return;
        }
        plugin.getServer().getConsoleSender().sendMessage(sendingPlayer.identity(), chatMessage, MessageType.CHAT);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void checkGlobalChatConditions(AsyncChatEvent event) {
        Player sendingPlayer = event.getPlayer();

        // Ignore when not prefixed with global prefix
        String uncoloredString = TextUtil.toSimpleString(event.message());
        if (!uncoloredString.startsWith(plugin.getSettings().getGlobalChatPrefix())) {
            return;
        }

        // Check permission to global chat
        if (!sendingPlayer.hasPermission(Permissions.GLOBAL_CHAT)) {
            sendingPlayer.sendMessage(plugin.getSettings().getNoPermission());
            event.setCancelled(true);
            return;
        }

        // Replace global prefix
        TextReplacementConfig prefixRemover =
                TextUtil.replaceText(plugin.getSettings().getGlobalChatPrefix(), Component.empty());
        String messageString = TextUtil.toSimpleString(event.message().replaceText(prefixRemover)).strip();

        // Check if message have any content
        if (messageString.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        // Check chat history and frequency
        PaperUser user = plugin.getUserManager().getUser(sendingPlayer.getUniqueId());
        ChatHistory.ChatResult chatResult = user.canSendGlobalMessage(messageString);
        handleChatResult(event, sendingPlayer, chatResult);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void sendGlobalChat(AsyncChatEvent event) {
        Player sendingPlayer = event.getPlayer();

        // Ignore when not prefixed with global prefix
        String uncoloredString = TextUtil.toSimpleString(event.message());
        if (!uncoloredString.startsWith(plugin.getSettings().getGlobalChatPrefix())) {
            return;
        }

        // Replace global prefix
        TextReplacementConfig prefixRemover =
                TextUtil.replaceText(plugin.getSettings().getGlobalChatPrefix(), Component.empty());
        Component messageComponent = event.message().replaceText(prefixRemover);
        String messageString = TextUtil.toSimpleString(messageComponent).strip();

        // Format chat
        Component chatMessage = TextUtil.applyChatFormat(plugin.getSettings().getGlobalChatFormat(),
                                                         sendingPlayer.getUniqueId(),
                                                         sendingPlayer.getName(),
                                                         messageComponent);
        // Cancel the event
        event.setCancelled(true);
        // Send the message to everybody
        plugin.getServer().sendMessage(sendingPlayer.identity(), chatMessage, MessageType.CHAT);
        // Store the message on the user's message history
        plugin.getUserManager().getUser(sendingPlayer.getUniqueId()).sentGlobalMessage(messageString);
    }
}
