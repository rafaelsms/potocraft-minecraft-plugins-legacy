package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager implements Listener {

    private final Object lock = new Object();
    private final Map<UUID, Profile> loadedProfiles = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull ServerProfilePlugin plugin;

    private @Nullable BukkitTask task = null;

    public UserManager(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void startSavingTask(ServerLoadEvent event) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        int time = plugin.getConfiguration().getSavePlayersTaskTimerTicks();
        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new SavePlayersTask(), time, time);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void loadProfile(AsyncPlayerPreLoginEvent event) {
        synchronized (lock) {
            try {
                Profile profile =
                        plugin.getDatabase().loadProfile(event.getUniqueId()).orElse(new Profile(event.getUniqueId()));
                loadedProfiles.put(event.getUniqueId(), profile);
            } catch (Database.DatabaseException ignored) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                               plugin.getConfiguration().getKickMessageCouldNotLoadProfile());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void insertProfile(PlayerJoinEvent event) {
        synchronized (lock) {
            Profile profile = loadedProfiles.remove(event.getPlayer().getUniqueId());
            if (profile == null) {
                event.getPlayer().kick(plugin.getConfiguration().getKickMessageCouldNotLoadProfile());
                return;
            }
            users.put(event.getPlayer().getUniqueId(), new User(plugin, event.getPlayer(), profile));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void removeProfile(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            synchronized (lock) {
                loadedProfiles.remove(event.getPlayer().getUniqueId());
                User removedUser = users.remove(event.getPlayer().getUniqueId());
                if (removedUser != null) {
                    Profile profile = removedUser.getProfile();
                    // Update play time
                    profile.setQuitTime();
                    plugin.getDatabase().saveProfileCatching(profile);
                }
            }
        }, 1);
    }

    public @NotNull User getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public @NotNull User getUser(@NotNull UUID playerId) {
        synchronized (lock) {
            return users.get(playerId);
        }
    }

    private class SavePlayersTask implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                // Simply save all profiles at once and hope it doesn't lag ;)
                for (User user : users.values()) {
                    plugin.getDatabase().saveProfileCatching(user.getProfile());
                }
            }
        }
    }
}
