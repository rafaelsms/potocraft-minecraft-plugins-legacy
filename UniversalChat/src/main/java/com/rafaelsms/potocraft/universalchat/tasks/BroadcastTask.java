package com.rafaelsms.potocraft.universalchat.tasks;

import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class BroadcastTask implements Runnable {

    private final @NotNull Random random = new Random();

    private final @NotNull UniversalChatPlugin plugin;

    public BroadcastTask(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<BaseComponent[]> broadcastMessages = plugin.getConfiguration().getBroadcastMessages();
        BaseComponent[] broadcast = broadcastMessages.get(random.nextInt(broadcastMessages.size()));
        // Send broadcast to all players
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            player.sendMessage(ChatMessageType.CHAT, broadcast);
        }
    }
}
