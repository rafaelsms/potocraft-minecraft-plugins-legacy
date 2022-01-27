package com.rafaelsms.potocraft.universalchat.commands;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.rafaelsms.potocraft.universalchat.util.ProxyUtil;
import com.rafaelsms.potocraft.util.ChatHistory;
import com.rafaelsms.potocraft.util.TextUtil;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageCommand implements RawCommand {

    private final Pattern commandSyntax = Pattern.compile("^\\s*(\\S+)\\s+(\\S+.*)$", Pattern.CASE_INSENSITIVE);
    private final Pattern tabCompletable = Pattern.compile("^\\s*\\S*$", Pattern.CASE_INSENSITIVE);

    private final @NotNull UniversalChatPlugin plugin;

    public MessageCommand(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            source.sendMessage(plugin.getConfiguration().getDirectMessagesHelp());
            return;
        }

        String username = matcher.group(1);
        String message = matcher.group(2);

        Optional<Player> optionalPlayer =
                TextUtil.closestMatch(plugin.getServer().getAllPlayers(), Player::getUsername, username);
        if (optionalPlayer.isEmpty()) {
            source.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }
        Player receiver = optionalPlayer.get();

        String senderUsername = plugin.getConfiguration().getConsoleName();
        Identity senderIdentity = Identity.nil();
        UUID senderId = null;
        if (source instanceof Player sender) {
            // Check if same player
            if (sender.getUniqueId().equals(receiver.getUniqueId())) {
                sender.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
                return;
            }

            senderUsername = sender.getUsername();
            senderIdentity = sender.identity();
            senderId = sender.getUniqueId();

            // Check if player is spamming
            User senderUser = plugin.getUserManager().getUser(sender.getUniqueId());
            ChatHistory chatHistory = senderUser.getChatHistory();
            ChatHistory.ChatResult chatResult = chatHistory.getSendMessageResult(message);
            switch (chatResult) {
                case SIMILAR_MESSAGES -> {
                    sender.sendMessage(plugin.getConfiguration().getMessagesTooSimilar());
                    return;
                }
                case TOO_FREQUENT -> {
                    sender.sendMessage(plugin.getConfiguration().getMessagesTooFrequent());
                    return;
                }
            }
            // Set message as sent if it wasn't cancelled
            chatHistory.sentMessage(message);
            // Set reply candidate for sender
            senderUser.setReplyCandidate(receiver.getUniqueId());
        }

        Component outgoingFormat =
                plugin.getConfiguration().getDirectMessagesOutgoingFormat(receiver.getUsername(), message);
        Component incomingFormat = plugin.getConfiguration().getDirectMessagesIncomingFormat(senderUsername, message);
        Component spyFormat =
                plugin.getConfiguration().getDirectMessagesSpyFormat(senderUsername, receiver.getUsername(), message);

        // Send to players
        source.sendMessage(senderIdentity, outgoingFormat, MessageType.CHAT);
        receiver.sendMessage(senderIdentity, incomingFormat, MessageType.CHAT);
        // Send to all spies
        for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
            if (onlinePlayer.hasPermission(Permissions.DIRECT_MESSAGES_SPY)) {
                // Skip of spy is the participant
                if (onlinePlayer.getUniqueId().equals(receiver.getUniqueId())) {
                    continue;
                }
                if (onlinePlayer.getUniqueId().equals(senderId)) {
                    continue;
                }
                onlinePlayer.sendMessage(senderIdentity, spyFormat, MessageType.CHAT);
            }
        }
        plugin.getLogger().info(TextUtil.toPlainString(spyFormat));

        // Update reply candidate for receiver
        User receiverUser = plugin.getUserManager().getUser(receiver.getUniqueId());
        if (senderId != null &&
            (receiverUser.getReplyCandidate().isEmpty() || receiverUser.getReplyCandidate().get().equals(senderId))) {
            receiverUser.setReplyCandidate(senderId);
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.DIRECT_MESSAGES);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        Matcher matcher = tabCompletable.matcher(invocation.arguments());
        if (matcher.matches()) {
            return ProxyUtil.getNameSuggester(plugin.getServer()).getSuggestions(invocation);
        }
        return List.of();
    }
}
