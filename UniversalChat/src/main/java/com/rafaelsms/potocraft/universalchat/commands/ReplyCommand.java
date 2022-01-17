package com.rafaelsms.potocraft.universalchat.commands;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.rafaelsms.potocraft.util.ChatHistory;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplyCommand implements RawCommand {

    private final Pattern commandSyntax = Pattern.compile("^\\s*(\\S+.*)$", Pattern.CASE_INSENSITIVE);

    private final @NotNull UniversalChatPlugin plugin;

    public ReplyCommand(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Ignore console
        if (!(invocation.source() instanceof Player sender)) {
            invocation.source().sendMessage(plugin.getConfiguration().getPlayersOnly());
            return;
        }
        User senderUser = plugin.getUserManager().getUser(sender.getUniqueId());

        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            sender.sendMessage(plugin.getConfiguration().getDirectMessagesReplyHelp());
            return;
        }

        String message = matcher.group(1);

        Optional<UUID> replyCandidateOptional = senderUser.getReplyCandidate();
        if (replyCandidateOptional.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }
        Optional<Player> receiverOptional = plugin.getServer().getPlayer(replyCandidateOptional.get());
        if (receiverOptional.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }
        Player receiver = receiverOptional.get();

        // Check if player is spamming
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

        Component outgoingFormat =
                plugin.getConfiguration().getDirectMessagesOutgoingFormat(receiver.getUsername(), message);
        Component incomingFormat =
                plugin.getConfiguration().getDirectMessagesIncomingFormat(sender.getUsername(), message);
        Component spyFormat = plugin
                .getConfiguration()
                .getDirectMessagesSpyFormat(sender.getUsername(), receiver.getUsername(), message);

        // Send to players
        sender.sendMessage(sender.identity(), outgoingFormat, MessageType.CHAT);
        receiver.sendMessage(sender.identity(), incomingFormat, MessageType.CHAT);
        // Send to all spies
        for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
            if (onlinePlayer.hasPermission(Permissions.DIRECT_MESSAGES_SPY)) {
                // Skip of spy is the participant
                if (onlinePlayer.getUniqueId().equals(receiver.getUniqueId())) {
                    continue;
                }
                if (onlinePlayer.getUniqueId().equals(sender.getUniqueId())) {
                    continue;
                }
                onlinePlayer.sendMessage(sender.identity(), spyFormat, MessageType.CHAT);
            }
        }
        plugin.getLogger().info(TextUtil.toPlainString(spyFormat));

        // Update reply candidate for receiver
        User receiverUser = plugin.getUserManager().getUser(receiver.getUniqueId());
        if (receiverUser.getReplyCandidate().isEmpty() ||
            receiverUser.getReplyCandidate().get().equals(sender.getUniqueId())) {
            receiverUser.setReplyCandidate(sender.getUniqueId());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.DIRECT_MESSAGES) &&
               invocation.source().hasPermission(Permissions.DIRECT_MESSAGES_REPLY);
    }
}
