package com.rafaelsms.potocraft.universalchat.listeners;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.rafaelsms.potocraft.util.ChatHistory;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChatListener {

    private final @NotNull UniversalChatPlugin plugin;

    public ChatListener(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    private void checkChatSpam(PlayerChatEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            // Ignore denied events
            if (!event.getResult().isAllowed()) {
                continuation.resume();
                return;
            }
            Player player = event.getPlayer();

            // Check if player have bypass permission
            if (player.hasPermission(Permissions.CHAT_BYPASS)) {
                continuation.resume();
                return;
            }

            // Check if message too small
            if (event.getMessage().length() < plugin.getConfiguration().getLimiterMinMessageLength()) {
                player.sendMessage(plugin.getConfiguration().getMessagesTooSmall());
                event.setResult(PlayerChatEvent.ChatResult.denied());
                continuation.resume();
                return;
            }

            // Check with old messages of user
            User user = plugin.getUserManager().getUser(player.getUniqueId());
            ChatHistory.ChatResult chatResult = user.getChatHistory().getSendMessageResult(event.getMessage());
            switch (chatResult) {
                case TOO_FREQUENT -> {
                    player.sendMessage(plugin.getConfiguration().getMessagesTooFrequent());
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                }
                case SIMILAR_MESSAGES -> {
                    player.sendMessage(plugin.getConfiguration().getMessagesTooSimilar());
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                }
                default -> {
                }
            }
            continuation.resume();
        });
    }

    @Subscribe(order = PostOrder.NORMAL)
    private void handleMessageSent(PlayerChatEvent event) {
        // Ignore denied events
        if (!event.getResult().isAllowed()) {
            return;
        }

        // Add message to history
        plugin.getUserManager()
              .getUser(event.getPlayer().getUniqueId())
              .getChatHistory()
              .sentMessage(event.getMessage());
    }

    @Subscribe(order = PostOrder.LATE)
    private void handleGlobalChat(PlayerChatEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            // Ignore denied events
            if (!event.getResult().isAllowed()) {
                continuation.resume();
                return;
            }

            // Check if starts with prefix
            String chatPrefix = plugin.getConfiguration().getGlobalChatPrefix();
            if (chatPrefix.isEmpty() || !event.getMessage().startsWith(chatPrefix)) {
                continuation.resume();
                return;
            }

            // Check player permission
            Player player = event.getPlayer();
            if (!player.hasPermission(Permissions.GLOBAL_CHAT)) {
                player.sendMessage(plugin.getConfiguration().getNoPermission());
                continuation.resume();
                return;
            }

            // Format the message
            String message = event.getMessage().replaceFirst(chatPrefix, "");
            Component chatMessage = plugin.getConfiguration().getGlobalChatFormat(player, message);
            Component spyMessage = plugin.getConfiguration().getGlobalChatSpyFormat(player, message);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            // Send the message for everybody of their server
            Optional<RegisteredServer> serverOptional = player.getCurrentServer().map(ServerConnection::getServer);
            if (serverOptional.isEmpty()) {
                continuation.resume();
                return;
            }
            RegisteredServer server = serverOptional.get();
            server.sendMessage(player.identity(), chatMessage, MessageType.CHAT);
            String serverName = server.getServerInfo().getName();
            // Check for any spy
            for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
                Optional<String> nameOptional = onlinePlayer.getCurrentServer()
                                                            .map(ServerConnection::getServer)
                                                            .map(RegisteredServer::getServerInfo)
                                                            .map(ServerInfo::getName);
                // Skip if player is on another server
                if (nameOptional.isPresent() && nameOptional.get().equalsIgnoreCase(serverName)) {
                    continue;
                }
                // Check if player has spy permission
                if (onlinePlayer.hasPermission(Permissions.GLOBAL_CHAT_SPY)) {
                    onlinePlayer.sendMessage(player.identity(), spyMessage, MessageType.CHAT);
                }
            }
            plugin.getLogger().info(TextUtil.toPlainString(spyMessage));
            continuation.resume();
        });
    }

    @Subscribe(order = PostOrder.LATE)
    private void handleUniversalChat(PlayerChatEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            // Ignore denied events
            if (!event.getResult().isAllowed()) {
                continuation.resume();
                return;
            }

            // Check if starts with prefix
            String chatPrefix = plugin.getConfiguration().getUniversalChatPrefix();
            if (chatPrefix.isEmpty() || !event.getMessage().startsWith(chatPrefix)) {
                continuation.resume();
                return;
            }

            // Check player permission
            Player player = event.getPlayer();
            if (!player.hasPermission(Permissions.UNIVERSAL_CHAT)) {
                player.sendMessage(plugin.getConfiguration().getNoPermission());
                continuation.resume();
                return;
            }

            // Format the message
            String message = event.getMessage().replaceFirst(chatPrefix, "");
            Component chatMessage = plugin.getConfiguration().getUniversalChatFormat(event.getPlayer(), message);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            // Send the message for everybody
            plugin.getServer().sendMessage(event.getPlayer().identity(), chatMessage, MessageType.CHAT);
            plugin.getLogger().info(TextUtil.toPlainString(chatMessage));
            continuation.resume();
        });
    }

    @Subscribe(order = PostOrder.LAST)
    private void handleAllChatSpy(PlayerChatEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            // Ignore denied events
            if (!event.getResult().isAllowed()) {
                continuation.resume();
                return;
            }

            // Format the message
            Player sendingPlayer = event.getPlayer();
            Component spyMessage =
                    plugin.getConfiguration().getOtherServerChatSpyFormat(sendingPlayer, event.getMessage());

            Optional<ServerConnection> currentServer = sendingPlayer.getCurrentServer();
            // Ignore if sender is on a unknown server
            if (currentServer.isEmpty()) {
                continuation.resume();
                return;
            }
            String currentServerName = currentServer.get().getServerInfo().getName();

            // Send spy message for those on other servers with spy permission
            for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
                if (onlinePlayer.getCurrentServer().isEmpty()) {
                    continue;
                }
                String onlinePlayerServerName = onlinePlayer.getCurrentServer().get().getServerInfo().getName();
                // Ignore same server
                if (currentServerName.equalsIgnoreCase(onlinePlayerServerName)) {
                    continue;
                }
                // Ignore if doesn't have the permission
                if (!onlinePlayer.hasPermission(Permissions.OTHER_SERVERS_CHAT_SPY)) {
                    continue;
                }
                onlinePlayer.sendMessage(spyMessage);
            }
            plugin.getLogger().info(TextUtil.toPlainString(spyMessage));
            continuation.resume();
        });
    }
}
