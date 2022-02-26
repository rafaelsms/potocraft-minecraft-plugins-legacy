package com.rafaelsms.potocraft.util;

import com.rafaelsms.potocraft.database.Database;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class UserManager<U, P> {

    private final Object lock = new Object();
    private final Set<UUID> leavingPlayers = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, P> loadedProfiles = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, U> users = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull JavaPlugin plugin;

    private final long savePlayerTaskPeriod;
    private @Nullable BukkitTask savePlayerTask = null;
    private @Nullable BukkitTask tickPlayerTask = null;

    protected UserManager(@NotNull JavaPlugin plugin, long savePlayerTaskPeriod) {
        this.plugin = plugin;
        this.savePlayerTaskPeriod = savePlayerTaskPeriod;
    }

    protected abstract Component getKickMessageCouldNotLoadProfile();

    protected abstract P retrieveProfile(AsyncPlayerPreLoginEvent event) throws Database.DatabaseException;

    protected abstract U retrieveUser(PlayerLoginEvent event, @NotNull P profile);

    protected abstract void onLogin(U user);

    protected abstract void onJoin(U user);

    protected abstract void onQuit(U user);

    protected abstract void tickUser(U user);

    protected abstract void saveUser(U user);

    public @NotNull UserManagerListener getListener() {
        return new UserManagerListener();
    }

    public @NotNull U getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public @NotNull U getUser(@NotNull UUID playerId) {
        synchronized (lock) {
            return users.get(playerId);
        }
    }

    public @NotNull Collection<U> getUsers() {
        synchronized (lock) {
            return Collections.unmodifiableCollection(users.values());
        }
    }

    private class TickPlayersTask implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                // Tick all users synchronously
                for (U user : users.values()) {
                    try {
                        tickUser(user);
                    } catch (Exception exception) {
                        plugin.getSLF4JLogger().error("Exception thrown on user tick task:", exception);
                    }
                }
            }
        }
    }

    private class SavePlayersTask implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                // Simply save all profiles at once and hope it doesn't lag ;)
                for (U user : users.values()) {
                    try {
                        saveUser(user);
                    } catch (Exception exception) {
                        plugin.getSLF4JLogger().error("Exception thrown on user save task:", exception);
                    }
                }
            }
        }
    }

    public class UserManagerListener implements Listener {

        private UserManagerListener() {
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void registerTasks(ServerLoadEvent event) {
            if (savePlayerTask != null) {
                savePlayerTask.cancel();
                savePlayerTask = null;
            }
            if (tickPlayerTask != null) {
                tickPlayerTask.cancel();
                tickPlayerTask = null;
            }
            savePlayerTask = plugin.getServer()
                                   .getScheduler()
                                   .runTaskTimerAsynchronously(plugin,
                                                               new SavePlayersTask(),
                                                               savePlayerTaskPeriod,
                                                               savePlayerTaskPeriod);
            tickPlayerTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new TickPlayersTask(), 1, 1);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void loadPlayerProfile(AsyncPlayerPreLoginEvent event) {
            try {
                P profile = retrieveProfile(event);
                synchronized (lock) {
                    if (leavingPlayers.contains(event.getUniqueId())) {
                        plugin.getSLF4JLogger()
                              .warn("Player joining before finished leaving: {} (uuid={})",
                                    event.getName(),
                                    event.getUniqueId());
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
                        return;
                    }
                    loadedProfiles.put(event.getUniqueId(), profile);
                }
            } catch (Database.DatabaseException ignored) {
                plugin.getSLF4JLogger()
                      .warn("Failed to load profile for {} (uuid={})", event.getName(), event.getUniqueId());
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void assignPlayerProfile(PlayerLoginEvent event) {
            synchronized (lock) {
                P profile = loadedProfiles.remove(event.getPlayer().getUniqueId());
                if (profile == null || leavingPlayers.contains(event.getPlayer().getUniqueId())) {
                    plugin.getSLF4JLogger()
                          .warn("Didn't have a loaded Profile for user {} (uuid = {})",
                                event.getPlayer().getName(), event.getPlayer().getUniqueId());
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
                    return;
                }
                U user = retrieveUser(event, profile);
                users.put(event.getPlayer().getUniqueId(), user);
                onLogin(user);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void executeJoinCallback(PlayerJoinEvent event) {
            synchronized (lock) {
                onJoin(getUser(event.getPlayer()));
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void removeUser(PlayerQuitEvent event) {
            final UUID playerId = event.getPlayer().getUniqueId();
            synchronized (lock) {
                leavingPlayers.add(playerId);
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                synchronized (lock) {
                    loadedProfiles.remove(playerId);
                    leavingPlayers.remove(playerId);
                    U removedUser = users.remove(playerId);
                    if (removedUser != null) {
                        try {
                            onQuit(removedUser);
                            saveUser(removedUser);
                        } catch (Exception exception) {
                            plugin.getSLF4JLogger().error("Exception thrown on user quit and save task:", exception);
                        }
                    }
                }
            });
        }
    }
}
