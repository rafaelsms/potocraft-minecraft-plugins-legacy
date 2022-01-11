package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import com.rafaelsms.potocraft.velocity.util.VelocityUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageCommand implements RawCommand {

    private final @NotNull VelocityPlugin plugin;

    public MessageCommand(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sendingSource = invocation.source();

        // Check if there is a message to reply
        String[] arguments = TextUtil.parseArguments(invocation.arguments());
        if (arguments.length <= 1) {
            sendingSource.sendMessage(plugin.getSettings().getCommandDirectMessageHelp());
            return;
        }
        String message = TextUtil.joinStrings(arguments, 1).strip();
        if (message.isEmpty()) {
            sendingSource.sendMessage(plugin.getSettings().getCommandDirectMessageHelp());
            return;
        }

        // Check if player is found and online
        String receivingPlayerName = arguments[0];
        Optional<Player> receivingPlayerOptional = VelocityUtil.searchPlayerName(plugin, receivingPlayerName);
        if (receivingPlayerOptional.isEmpty()) {
            sendingSource.sendMessage(plugin.getSettings().getPlayerNotFound());
            return;
        }
        Player receivingPlayer = receivingPlayerOptional.get();
        if (!receivingPlayer.isActive()) {
            sendingSource.sendMessage(plugin.getSettings().getCommandDirectMessageRecipientLeft());
            return;
        }

        Identity sendingPlayerIdentity = Identity.nil();
        Component sendingPlayerUsername = plugin.getSettings().getConsoleName();
        Component messageComponent = Component.text(message);
        // Update values and check send limit (if source is a player)
        if (sendingSource instanceof Player sendingPlayer) {
            sendingPlayerIdentity = sendingPlayer.identity();
            sendingPlayerUsername = Component.text(sendingPlayer.getUsername());

            // Check limits
            VelocityUser sendingUser = plugin.getUserManager().getUser(sendingPlayer.getUniqueId());
            ChatHistory.ChatResult chatResult = sendingUser.canSendDirectMessages(message);
            if (!chatResult.isAllowed()) {
                if (chatResult == ChatHistory.ChatResult.TOO_FREQUENT) {
                    sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooFrequent());
                } else if (chatResult == ChatHistory.ChatResult.SIMILAR_MESSAGES) {
                    sendingPlayer.sendMessage(plugin.getSettings().getChatMessagesTooSimilar());
                }
                return;
            }
        }

        // Apply formats
        Component incomingMessage =
                plugin.getSettings().getDirectMessageIncomingFormat(sendingPlayerUsername, messageComponent);
        Component outgoingMessage = plugin
                .getSettings()
                .getDirectMessageOutgoingFormat(Component.text(receivingPlayer.getUsername()), messageComponent);
        Component spyMessage = plugin
                .getSettings()
                .getDirectMessageSpyFormat(sendingPlayerUsername,
                                           Component.text(receivingPlayer.getUsername()),
                                           messageComponent);
        // Send message to the player
        receivingPlayer.sendMessage(sendingPlayerIdentity, incomingMessage, MessageType.CHAT);
        sendingSource.sendMessage(sendingPlayerIdentity, outgoingMessage, MessageType.CHAT);
        // Send to those who spy
        for (Player player : plugin.getProxyServer().getAllPlayers()) {
            if (!player.hasPermission(Permissions.MESSAGE_COMMAND_SPY)) {
                continue;
            }
            player.sendMessage(sendingPlayerIdentity, spyMessage, MessageType.CHAT);
        }
        plugin
                .getProxyServer()
                .getConsoleCommandSource()
                .sendMessage(sendingPlayerIdentity, spyMessage, MessageType.CHAT);

        // Update reply candidates for both (if source is a player)
        if (sendingSource instanceof Player sendingPlayer) {
            VelocityUser sendingUser = plugin.getUserManager().getUser(sendingPlayer.getUniqueId());
            sendingUser.setLastReplyCandidate(receivingPlayer.getUniqueId());
            // Set last reply candidate if none or if the same player (keep the old one if it isn't this player)
            VelocityUser receivingUser = plugin.getUserManager().getUser(receivingPlayer.getUniqueId());
            Optional<UUID> receivingReplyCandidate = receivingUser.getLastReplyCandidate();
            if (receivingReplyCandidate.isEmpty() ||
                receivingReplyCandidate.get().equals(sendingPlayer.getUniqueId())) {
                receivingUser.setLastReplyCandidate(sendingPlayer.getUniqueId());
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (TextUtil.parseArguments(invocation.arguments()).length == 0) {
            return VelocityUtil.getPlayerNameList(plugin);
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.MESSAGE_COMMAND);
    }
}
