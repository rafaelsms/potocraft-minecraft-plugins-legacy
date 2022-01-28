package com.rafaelsms.potocraft.players;

import com.rafaelsms.potocraft.PaperPlugin;
import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.database.pojo.ServerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

public class ServerUserManager extends UserManager<Player, ServerPlayer, ServerProfile, ServerUser>
        implements Listener {

    private final PaperPlugin plugin;

    public ServerUserManager(PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ServerUser getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
        ServerProfile serverProfile;
        try {
            serverProfile = plugin.getDatabase().getPlayerObject(event.getUniqueId()).orElse(new ServerProfile());
        } catch (Database.DatabaseException ignored) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "a");
            return;
        }

    }
}
