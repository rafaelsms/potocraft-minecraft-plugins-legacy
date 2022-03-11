package com.rafaelsms.potocraft.universalchat.commands;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.rafaelsms.potocraft.universalchat.util.ChatUtil;
import com.rafaelsms.potocraft.util.ChatHistory;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageCommand extends Command implements TabExecutor {

    private final @NotNull UniversalChatPlugin plugin;

    public MessageCommand(@NotNull UniversalChatPlugin plugin) {
        super("mensagem", Permissions.DIRECT_MESSAGES, "message", "msg", "tell", "dm", "pm", "w", "whisper");
        this.plugin = plugin;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(Permissions.DIRECT_MESSAGES);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfiguration().getDirectMessagesHelp());
            return;
        }

        String username = args[0];
        Optional<String> messageOptional = TextUtil.joinStrings(args, 1);
        if (messageOptional.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getDirectMessagesHelp());
            return;
        }
        String message = messageOptional.get();
        message = plugin.getWordsChecker().removeBlockedWords(message).orElse(message);

        Optional<ProxiedPlayer> optionalPlayer =
                TextUtil.closestMatch(plugin.getProxy().getPlayers(), ProxiedPlayer::getName, username);
        if (optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }
        ProxiedPlayer receiver = optionalPlayer.get();

        String senderUsername = plugin.getConfiguration().getConsoleName();
        UUID senderId = null;
        if (sender instanceof ProxiedPlayer player) {
            // Check if same player
            if (player.getUniqueId().equals(receiver.getUniqueId())) {
                player.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
                return;
            }

            senderUsername = player.getName();
            senderId = player.getUniqueId();

            // Check if player is spamming
            User senderUser = plugin.getUserManager().getUser(player.getUniqueId());
            ChatHistory chatHistory = senderUser.getChatHistory();
            ChatHistory.ChatResult chatResult = chatHistory.getSendMessageResult(message);
            switch (chatResult) {
                case SIMILAR_MESSAGES -> {
                    player.sendMessage(plugin.getConfiguration().getMessagesTooSimilar());
                    return;
                }
                case TOO_FREQUENT -> {
                    player.sendMessage(plugin.getConfiguration().getMessagesTooFrequent());
                    return;
                }
            }

            // Set message as sent if it wasn't cancelled
            chatHistory.sentMessage(message);

            // Set reply candidate for sender
            senderUser.setReplyCandidate(receiver.getUniqueId());
        }

        Component messageComponent = ChatUtil.parseMessage(sender, message);
        @NotNull BaseComponent[] outgoingFormat =
                plugin.getConfiguration().getDirectMessagesOutgoingFormat(receiver.getName(), messageComponent);
        @NotNull BaseComponent[] incomingFormat =
                plugin.getConfiguration().getDirectMessagesIncomingFormat(senderUsername, messageComponent);
        @NotNull BaseComponent[] spyFormat = plugin.getConfiguration()
                                                   .getDirectMessagesSpyFormat(senderUsername,
                                                                               receiver.getName(),
                                                                               messageComponent);

        // Send to players
        if (senderId != null) {
            ((ProxiedPlayer) sender).sendMessage(senderId, outgoingFormat);
        }
        receiver.sendMessage(senderId, incomingFormat);
        // Send to all spies
        for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
            if (onlinePlayer.hasPermission(Permissions.DIRECT_MESSAGES_SPY)) {
                // Skip of spy is the participant
                if (onlinePlayer.getUniqueId().equals(receiver.getUniqueId())) {
                    continue;
                }
                if (onlinePlayer.getUniqueId().equals(senderId)) {
                    continue;
                }
                if (senderId != null) {
                    onlinePlayer.sendMessage(senderId, spyFormat);
                } else {
                    onlinePlayer.sendMessage(spyFormat);
                }
            }
        }
        plugin.getProxy().getConsole().sendMessage(spyFormat);

        // Update reply candidate for receiver
        User receiverUser = plugin.getUserManager().getUser(receiver.getUniqueId());
        if (senderId != null &&
            (receiverUser.getReplyCandidate().isEmpty() || receiverUser.getReplyCandidate().get().equals(senderId))) {
            receiverUser.setReplyCandidate(senderId);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Util.convertList(plugin.getProxy().getPlayers(), ProxiedPlayer::getName);
        }
        return List.of();
    }
}
