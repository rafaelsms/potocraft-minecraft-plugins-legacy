package com.rafaelsms.potocraft.universalchat.listeners;

import com.rafaelsms.potocraft.universalchat.UniversalChatPlugin;
import com.rafaelsms.potocraft.universalchat.player.User;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private final @NotNull Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull UniversalChatPlugin plugin;

    public UserManager(@NotNull UniversalChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    private void createUserProfile(PostLoginEvent event) {
        users.put(event.getPlayer().getUniqueId(), new User(plugin, event.getPlayer()));
    }

    @Subscribe(order = PostOrder.LAST)
    private void removeUserProfile(DisconnectEvent event) {
        users.remove(event.getPlayer().getUniqueId());
    }

    public @NotNull User getUser(@NotNull UUID playerId) {
        return users.get(playerId);
    }
}
