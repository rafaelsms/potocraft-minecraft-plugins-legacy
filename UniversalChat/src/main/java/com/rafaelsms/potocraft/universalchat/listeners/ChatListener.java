package com.rafaelsms.potocraft.universalchat.listeners;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.rafaelsms.potocraft.universalchat.util.ChatUtil;
import com.rafaelsms.potocraft.util.ChatHistory;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChatListener implements Listener {

    private final @NotNull UniversalChatPlugin plugin;

    public ChatListener(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkChatSpam(ChatEvent event) {
        // Ignore cancelled events and commands
        if (event.isCancelled() || event.isCommand()) {
            return;
        }
        // Ignore non-players
        if (!(event.getSender() instanceof ProxiedPlayer player)) {
            return;
        }

        // Check if player have bypass permission
        if (player.hasPermission(Permissions.CHAT_BYPASS)) {
            return;
        }

        // Check if message too small
        if (event.getMessage().length() < plugin.getConfiguration().getLimiterMinMessageLength()) {
            player.sendMessage(plugin.getConfiguration().getMessagesTooSmall());
            event.setCancelled(true);
            return;
        }

        // Check with old messages of user
        User user = plugin.getUserManager().getUser(player.getUniqueId());
        ChatHistory.ChatResult chatResult = user.getChatHistory().getSendMessageResult(event.getMessage());
        switch (chatResult) {
            case TOO_FREQUENT -> {
                player.sendMessage(plugin.getConfiguration().getMessagesTooFrequent());
                event.setCancelled(true);
            }
            case SIMILAR_MESSAGES -> {
                player.sendMessage(plugin.getConfiguration().getMessagesTooSimilar());
                event.setCancelled(true);
            }
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkBlockedWords(ChatEvent event) {
        // Ignore cancelled events and commands
        if (event.isCancelled() || event.isCommand()) {
            return;
        }
        // Remove any blocked words very early and for every chat
        event.setMessage(plugin.getWordsChecker().removeBlockedWords(event.getMessage()).orElse(event.getMessage()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void handleMessageSent(ChatEvent event) {
        // Ignore cancelled events and commands
        if (event.isCancelled() || event.isCommand()) {
            return;
        }
        // Ignore non-players
        if (!(event.getSender() instanceof ProxiedPlayer player)) {
            return;
        }

        // Add message to history
        plugin.getUserManager().getUser(player.getUniqueId()).getChatHistory().sentMessage(event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleGlobalChat(ChatEvent event) {
        // Ignore cancelled events and commands
        if (event.isCancelled() || event.isCommand()) {
            return;
        }
        // Ignore non-players
        if (!(event.getSender() instanceof ProxiedPlayer player)) {
            return;
        }

        // Check if starts with prefix
        String chatPrefix = plugin.getConfiguration().getGlobalChatPrefix();
        if (chatPrefix.isEmpty() || !event.getMessage().startsWith(chatPrefix)) {
            return;
        }

        // Check player permission
        if (!player.hasPermission(Permissions.GLOBAL_CHAT)) {
            player.sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        // Format the message
        String message = event.getMessage().replaceFirst(chatPrefix, "");
        Component messageComponent = ChatUtil.parseMessage(player, message);
        @NotNull BaseComponent[] chatMessage = plugin.getConfiguration().getGlobalChatFormat(player, messageComponent);
        @NotNull BaseComponent[] spyMessage =
                plugin.getConfiguration().getGlobalChatSpyFormat(player, messageComponent);
        event.setCancelled(true);

        // Send the message for everybody of their server
        Optional<ServerInfo> serverOptional = Optional.ofNullable(player.getServer()).map(Server::getInfo);
        if (serverOptional.isEmpty()) {
            return;
        }
        ServerInfo server = serverOptional.get();
        for (ProxiedPlayer serverPlayer : server.getPlayers()) {
            serverPlayer.sendMessage(chatMessage);
        }
        String serverName = server.getName();
        // Check for any spy
        for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
            Optional<String> nameOptional =
                    Optional.ofNullable(onlinePlayer.getServer()).map(Server::getInfo).map(ServerInfo::getName);
            // Skip if player is on another server
            if (nameOptional.isPresent() && nameOptional.get().equalsIgnoreCase(serverName)) {
                continue;
            }
            // Check if player has spy permission
            if (onlinePlayer.hasPermission(Permissions.GLOBAL_CHAT_SPY)) {
                onlinePlayer.sendMessage(spyMessage);
            }
        }
        plugin.getProxy().getConsole().sendMessage(spyMessage);
    }

    @EventHandler(priority = EventPriority.NORMAL) // Highest priority than global chat
    public void handleUniversalChat(ChatEvent event) {
        // Ignore cancelled events and commands
        if (event.isCancelled() || event.isCommand()) {
            return;
        }
        // Ignore non-players
        if (!(event.getSender() instanceof ProxiedPlayer player)) {
            return;
        }

        // Check if starts with prefix
        String chatPrefix = plugin.getConfiguration().getUniversalChatPrefix();
        if (chatPrefix.isEmpty() || !event.getMessage().startsWith(chatPrefix)) {
            return;
        }

        // Check player permission
        if (!player.hasPermission(Permissions.UNIVERSAL_CHAT)) {
            player.sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        // Format the message
        String message = event.getMessage().replaceFirst(chatPrefix, "");
        Component messageComponent = ChatUtil.parseMessage(player, message);
        @NotNull BaseComponent[] chatMessage =
                plugin.getConfiguration().getUniversalChatFormat(player, messageComponent);
        event.setCancelled(true);

        // Send the message for everybody
        for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
            onlinePlayer.sendMessage(chatMessage);
        }
        plugin.getProxy().getConsole().sendMessage(chatMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleAllChatSpy(ChatEvent event) {
        // Ignore cancelled events and commands
        if (event.isCancelled() || event.isCommand()) {
            return;
        }
        // Ignore non-players
        if (!(event.getSender() instanceof ProxiedPlayer sendingPlayer)) {
            return;
        }

        // Format the message
        Component messageComponent = ChatUtil.parseMessage(sendingPlayer, event.getMessage());
        @NotNull BaseComponent[] spyMessage =
                plugin.getConfiguration().getOtherServerChatSpyFormat(sendingPlayer, messageComponent);

        Optional<ServerInfo> currentServer = Optional.ofNullable(sendingPlayer.getServer()).map(Server::getInfo);
        // Ignore if sender is on a unknown server
        if (currentServer.isEmpty()) {
            return;
        }
        String currentServerName = currentServer.get().getName();

        // Send spy message for those on other servers with spy permission
        for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
            Optional<ServerInfo> onlinePlayerServer =
                    Optional.ofNullable(onlinePlayer.getServer()).map(Server::getInfo);
            if (onlinePlayerServer.isEmpty()) {
                continue;
            }
            String onlinePlayerServerName = onlinePlayerServer.get().getName();
            // Ignore same server
            if (currentServerName.equalsIgnoreCase(onlinePlayerServerName)) {
                continue;
            }
            // Ignore if player doesn't have the permission
            if (!onlinePlayer.hasPermission(Permissions.OTHER_SERVERS_CHAT_SPY)) {
                continue;
            }
            onlinePlayer.sendMessage(spyMessage);
        }
        plugin.getProxy().getConsole().sendMessage(spyMessage);
    }
}
