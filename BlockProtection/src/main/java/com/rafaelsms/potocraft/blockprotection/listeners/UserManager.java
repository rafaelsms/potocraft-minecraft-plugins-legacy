package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.Profile;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.database.Database;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    private final @NotNull Map<UUID, Profile> profileMap = Collections.synchronizedMap(new HashMap<>());
    private final @NotNull HashMap<UUID, User> userMap = new HashMap<>();

    public UserManager(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void fetchUser(AsyncPlayerPreLoginEvent event) {
        UUID playerId = event.getUniqueId();
        Profile profile;
        try {
            profile = plugin.getDatabase().getProfile(playerId).orElse(new Profile(plugin, playerId));
            synchronized (profileMap) {
                profileMap.put(playerId, profile);
            }
        } catch (Database.DatabaseException ignored) {
            Component message = plugin.getConfiguration().getFailedToFetchProfile();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void insertUser(PlayerJoinEvent event) {
        synchronized (profileMap) {
            Profile profile = profileMap.get(event.getPlayer().getUniqueId());
            if (profile == null) {
                event.getPlayer().kick(plugin.getConfiguration().getFailedToFetchProfile());
                return;
            }
            profile.setJoinDate();
            userMap.put(event.getPlayer().getUniqueId(), new User(plugin, event.getPlayer(), profile));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void removeUser(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            synchronized (profileMap) {
                profileMap.remove(event.getPlayer().getUniqueId());
                User removedUser = userMap.remove(event.getPlayer().getUniqueId());
                if (removedUser != null) {
                    Profile profile = removedUser.getProfile();
                    plugin.getDatabase().saveProfileCatching(profile);
                }
            }
        });
    }

    public @NotNull User getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(@NotNull UUID playerId) {
        return userMap.get(playerId);
    }
}
