package com.rafaelsms.potocraft.plugin.player;

import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.plugin.BaseConfiguration;
import com.rafaelsms.potocraft.plugin.BaseJavaPlugin;
import com.rafaelsms.potocraft.plugin.BasePermissions;
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

public abstract class BaseUserManager<User extends BaseUser<Profile>, Profile extends BaseProfile> {

    private final Object lock = new Object();
    private final Set<UUID> leavingPlayers = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Profile> loadedProfiles = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull BaseJavaPlugin<User, Profile, ? extends BaseDatabase, ? extends BaseConfiguration, ? extends BasePermissions>
            plugin;
    private final @NotNull UserManagerListener listener;

    private final long savePlayerTaskPeriod;
    private @Nullable BukkitTask savePlayerTask = null;
    private @Nullable BukkitTask tickPlayerTask = null;

    public BaseUserManager(@NotNull BaseJavaPlugin<User, Profile, ? extends BaseDatabase, ? extends BaseConfiguration, ? extends BasePermissions> plugin) {
        this.plugin = plugin;
        this.savePlayerTaskPeriod = plugin.getConfiguration().getProfileSavingTaskTimer();
        this.listener = new UserManagerListener();
    }

    protected abstract Component getKickMessageCouldNotLoadProfile();

    protected abstract @NotNull Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws DatabaseException;

    protected abstract @NotNull User retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) throws
                                                                                                    DatabaseException;

    protected abstract void onLogin(@NotNull User user);

    protected abstract void onJoin(@NotNull User user);

    protected abstract void onQuit(@NotNull User user);

    protected void tickUser(@NotNull User user) {
        user.tick();
    }

    protected void saveUser(@NotNull User user) throws DatabaseException {
        user.getProfile().save();
    }

    public @NotNull UserManagerListener getListener() {
        return listener;
    }

    private @NotNull User getMappedUser(@NotNull UUID playerId) throws DatabaseException {
        synchronized (lock) {
            User user = users.get(playerId);
            if (user == null) {
                throw new DatabaseException("User profile for player \"%s\" is null!".formatted(playerId.toString()));
            }
            return user;
        }
    }

    private void kickPlayerForNullUser(@NotNull Player player) {
        plugin.logger()
              .error("Failed to retrieve User profile for online player {} (UUID = {})",
                     player.getName(),
                     player.getUniqueId());
        player.kick(plugin.getConfiguration().getFailedToRetrieveUserProfile());
    }

    public @NotNull User getUser(@NotNull Player player) {
        try {
            return getMappedUser(player.getUniqueId());
        } catch (DatabaseException exception) {
            kickPlayerForNullUser(player);
            throw new NullPointerException(exception.getLocalizedMessage());
        }
    }

    public @NotNull User getUser(@NotNull UUID playerId) {
        try {
            return getMappedUser(playerId);
        } catch (DatabaseException exception) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                kickPlayerForNullUser(player);
            } else {
                plugin.logger().error("Failed to retrieve User profile for offline UUID = \"{}\"", playerId);
            }
            throw new NullPointerException(exception.getLocalizedMessage());
        }
    }

    public @NotNull Collection<User> getUsers() {
        synchronized (lock) {
            return Collections.unmodifiableCollection(users.values());
        }
    }

    private class TickPlayersTask implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                // Tick all users synchronously
                for (User user : users.values()) {
                    try {
                        tickUser(user);
                    } catch (Exception exception) {
                        plugin.logger().error("Exception thrown on user tick task:", exception);
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
                for (User user : users.values()) {
                    try {
                        saveUser(user);
                    } catch (DatabaseException exception) {
                        plugin.logger().warn("Failed to save user:", exception);
                    } catch (Exception exception) {
                        plugin.logger().error("Exception thrown on user save task:", exception);
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
                Profile profile = retrieveProfile(event);
                synchronized (lock) {
                    if (leavingPlayers.contains(event.getUniqueId())) {
                        plugin.logger()
                              .warn("Player joining before finished leaving: {} (uuid={})",
                                    event.getName(),
                                    event.getUniqueId());
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
                        return;
                    }
                    loadedProfiles.put(event.getUniqueId(), profile);
                }
            } catch (DatabaseException ignored) {
                plugin.logger().warn("Failed to load profile for {} (uuid={})", event.getName(), event.getUniqueId());
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void assignPlayerProfile(PlayerLoginEvent event) {
            synchronized (lock) {
                Profile profile = loadedProfiles.remove(event.getPlayer().getUniqueId());
                if (profile == null || leavingPlayers.contains(event.getPlayer().getUniqueId())) {
                    plugin.logger()
                          .warn("Didn't have a loaded Profile for user {} (uuid = {})",
                                event.getPlayer().getName(),
                                event.getPlayer().getUniqueId());
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
                    return;
                }
                try {
                    User user = retrieveUser(event, profile);
                    users.put(event.getPlayer().getUniqueId(), user);
                    onLogin(user);
                } catch (DatabaseException ignored) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
                } catch (Exception exception) {
                    plugin.logger()
                          .warn("Failed to assign profile for user {} (uuid = {}): {}",
                                event.getPlayer().getName(),
                                event.getPlayer().getUniqueId(),
                                exception.getLocalizedMessage());
                    exception.printStackTrace();
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, getKickMessageCouldNotLoadProfile());
                }
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
                    User removedUser = users.remove(playerId);
                    if (removedUser != null) {
                        try {
                            onQuit(removedUser);
                            saveUser(removedUser);
                        } catch (DatabaseException exception) {
                            plugin.logger().warn("Failed to save user:", exception);
                        } catch (Exception exception) {
                            plugin.logger().error("Exception thrown on user quit and save task:", exception);
                        }
                    }
                }
            });
        }
    }
}
