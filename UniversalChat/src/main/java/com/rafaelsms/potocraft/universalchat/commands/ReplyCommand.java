package com.rafaelsms.potocraft.universalchat.commands;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.rafaelsms.potocraft.universalchat.util.ChatUtil;
import com.rafaelsms.potocraft.util.ChatHistory;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommand extends Command {

    private final @NotNull UniversalChatPlugin plugin;

    public ReplyCommand(@NotNull UniversalChatPlugin plugin) {
        super("responder", Permissions.DIRECT_MESSAGES_REPLY, "r", "reply");
        this.plugin = plugin;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(Permissions.DIRECT_MESSAGES) &&
               sender.hasPermission(Permissions.DIRECT_MESSAGES_REPLY);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Ignore console
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayersOnly());
            return;
        }
        User senderUser = plugin.getUserManager().getUser(player.getUniqueId());

        if (args.length == 0) {
            player.sendMessage(plugin.getConfiguration().getDirectMessagesReplyHelp());
            return;
        }

        Optional<String> stringOptional = TextUtil.joinStrings(args, 0);
        if (stringOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getDirectMessagesReplyHelp());
            return;
        }
        String message = stringOptional.get();
        message = plugin.getWordsChecker().removeBlockedWords(message).orElse(message);

        Optional<UUID> replyCandidateOptional = senderUser.getReplyCandidate();
        if (replyCandidateOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }
        Optional<ProxiedPlayer> receiverOptional =
                Optional.ofNullable(plugin.getProxy().getPlayer(replyCandidateOptional.get()));
        if (receiverOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }
        ProxiedPlayer receiver = receiverOptional.get();

        // Check if same player
        if (player.getUniqueId().equals(receiver.getUniqueId())) {
            player.sendMessage(plugin.getConfiguration().getDirectMessagesNoPlayerFound());
            return;
        }

        // Check if player is spamming
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

        Component messageComponent = ChatUtil.parseMessage(sender, message);
        @NotNull BaseComponent[] outgoingFormat =
                plugin.getConfiguration().getDirectMessagesOutgoingFormat(receiver.getName(), messageComponent);
        @NotNull BaseComponent[] incomingFormat =
                plugin.getConfiguration().getDirectMessagesIncomingFormat(player.getName(), messageComponent);
        @NotNull BaseComponent[] spyFormat = plugin.getConfiguration()
                                                   .getDirectMessagesSpyFormat(player.getName(),
                                                                               receiver.getName(),
                                                                               messageComponent);

        // Send to players
        player.sendMessage(player.getUniqueId(), outgoingFormat);
        receiver.sendMessage(player.getUniqueId(), incomingFormat);
        // Send to all spies
        for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
            if (onlinePlayer.hasPermission(Permissions.DIRECT_MESSAGES_SPY)) {
                // Skip of spy is the participant
                if (onlinePlayer.getUniqueId().equals(receiver.getUniqueId())) {
                    continue;
                }
                if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                onlinePlayer.sendMessage(player.getUniqueId(), spyFormat);
            }
        }
        plugin.getProxy().getConsole().sendMessage(spyFormat);

        // Update reply candidate for receiver
        User receiverUser = plugin.getUserManager().getUser(receiver.getUniqueId());
        if (receiverUser.getReplyCandidate().isEmpty() ||
            receiverUser.getReplyCandidate().get().equals(player.getUniqueId())) {
            receiverUser.setReplyCandidate(player.getUniqueId());
        }
    }
}
