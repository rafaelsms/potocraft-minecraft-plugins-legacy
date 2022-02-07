package com.rafaelsms.potocraft.universalchat.listeners;

import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager implements Listener {

    private final @NotNull Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull UniversalChatPlugin plugin;

    public UserManager(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void createUserProfile(PostLoginEvent event) {
        users.put(event.getPlayer().getUniqueId(), new User(plugin, event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void removeUserProfile(PlayerDisconnectEvent event) {
        users.remove(event.getPlayer().getUniqueId());
    }

    public @NotNull User getUser(@NotNull UUID playerId) {
        return users.get(playerId);
    }
}
