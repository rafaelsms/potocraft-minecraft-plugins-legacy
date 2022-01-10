package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatController implements Listener {

    private final @NotNull PaperPlugin plugin;

    public ChatController(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onLocalChat(AsyncChatEvent event) {
        // TODO filter to just nearby people if not preceded with !
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onGlobalChat(AsyncChatEvent event) {
        // TODO filter to just nearby people if not preceded with !
    }
}
