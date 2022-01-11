package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReplyCommand implements RawCommand {

    private final @NotNull VelocityPlugin plugin;

    public ReplyCommand(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Check if is a player (supports User and therefore can store reply candidates)
        if (!(invocation.source() instanceof Player sendingPlayer)) {
            invocation.source().sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return;
        }

        // Check if there is a message to reply
        if (TextUtil.parseArguments(invocation.arguments()).length == 0) {
            sendingPlayer.sendMessage(plugin.getSettings().getCommandDirectMessageReplyHelp());
            return;
        }
        String message = invocation.arguments().strip();
        if (message.isEmpty()) {
            sendingPlayer.sendMessage(plugin.getSettings().getCommandDirectMessageReplyHelp());
            return;
        }

        // Check if the player user has a reply candidate
        VelocityUser sendingUser = plugin.getUserManager().getUser(sendingPlayer.getUniqueId());
        Optional<UUID> receivingIdOptional = sendingUser.getLastReplyCandidate();
        if (receivingIdOptional.isEmpty()) {
            sendingPlayer.sendMessage(plugin.getSettings().getCommandDirectMessageNoRecipient());
            return;
        }

        // Apply chat limiter
        ChatHistory.ChatResult chatResult = sendingUser.canSendDirectMessages(message);
        if (!chatResult.isAllowed()) {
            if (chatResult == ChatHistory.ChatResult.TOO_FREQUENT) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooFrequent());
            } else if (chatResult == ChatHistory.ChatResult.SIMILAR_MESSAGES) {
                sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooSimilar());
            }
            return;
        }

        // Get receiving player instance
        UUID receivingId = receivingIdOptional.get();
        Optional<Player> receivingOptional = plugin.getProxyServer().getPlayer(receivingId);
        if (receivingOptional.isEmpty()) {
            sendingPlayer.sendMessage(plugin.getSettings().getCommandDirectMessageNoRecipient());
            return;
        }
        Player receivingPlayer = receivingOptional.get();
        if (!receivingPlayer.isActive()) {
            sendingPlayer.sendMessage(plugin.getSettings().getCommandDirectMessageRecipientLeft());
            return;
        }

        // Apply formats
        Component sendingPlayerUsername = Component.text(sendingPlayer.getUsername());
        Component receivingPlayerName = Component.text(receivingPlayer.getUsername());
        Component messageComponent = Component.text(message);
        Component incomingMessage =
                plugin.getSettings().getDirectMessageIncomingFormat(sendingPlayerUsername, messageComponent);
        Component outgoingMessage =
                plugin.getSettings().getDirectMessageOutgoingFormat(receivingPlayerName, messageComponent);
        Component spyMessage = plugin
                .getSettings()
                .getDirectMessageSpyFormat(sendingPlayerUsername, receivingPlayerName, messageComponent);
        // Send message to the player
        receivingPlayer.sendMessage(sendingPlayer.identity(), incomingMessage, MessageType.CHAT);
        sendingPlayer.sendMessage(sendingPlayer.identity(), outgoingMessage, MessageType.CHAT);
        // Send to those who spy
        for (Player player : plugin.getProxyServer().getAllPlayers()) {
            if (!player.hasPermission(Permissions.MESSAGE_COMMAND_SPY)) {
                continue;
            }
            player.sendMessage(sendingPlayer.identity(), spyMessage, MessageType.CHAT);
        }
        plugin
                .getProxyServer()
                .getConsoleCommandSource()
                .sendMessage(sendingPlayer.identity(), spyMessage, MessageType.CHAT);
        // Update reply candidates for both
        VelocityUser receivingUser = plugin.getUserManager().getUser(receivingPlayer.getUniqueId());
        sendingUser.setLastReplyCandidate(receivingPlayer.getUniqueId());
        // Set last reply candidate if none or if the same player (keep the old one if it isn't this player)
        Optional<UUID> receivingReplyCandidate = receivingUser.getLastReplyCandidate();
        if (receivingReplyCandidate.isEmpty() || receivingReplyCandidate.get().equals(sendingPlayer.getUniqueId())) {
            receivingUser.setLastReplyCandidate(sendingPlayer.getUniqueId());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        boolean hasMessagePermission = invocation.source().hasPermission(Permissions.MESSAGE_COMMAND);
        boolean hasReplyPermission = invocation.source().hasPermission(Permissions.MESSAGE_COMMAND_REPLY);
        return hasMessagePermission && hasReplyPermission;
    }
}
