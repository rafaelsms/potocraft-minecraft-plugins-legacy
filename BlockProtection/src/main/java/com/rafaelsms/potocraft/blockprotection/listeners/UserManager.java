package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class UserManager implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    private final @NotNull HashMap<UUID, User> userMap = new HashMap<>();

    public UserManager(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void insertUser(PlayerJoinEvent event) {
        userMap.put(event.getPlayer().getUniqueId(), new User(plugin, event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void removeUser(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            userMap.remove(event.getPlayer().getUniqueId());
        });
    }

    public @NotNull User getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(@NotNull UUID playerId) {
        return userMap.get(playerId);
    }
}
