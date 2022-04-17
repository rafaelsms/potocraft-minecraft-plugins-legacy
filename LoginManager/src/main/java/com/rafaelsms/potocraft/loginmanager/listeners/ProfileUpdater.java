package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.LoginUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * We will: - check if offline profiles are still logged in; - update offline profiles' login date if needed; - update
 * offline profiles' logged in status if needed; - validate profiles' logged in status on startup; - update join/quit
 * date for online players.
 */
public class ProfileUpdater implements Listener {

    private final @NotNull LoginManagerPlugin plugin;

    public ProfileUpdater(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    // Wait for our PlayerTypeManager to register
    @EventHandler(priority = EventPriority.LOW)
    public void updateJoinDate(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        boolean playerTypeRequiresLogin = plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin();
        Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
        // Don't create profile for offline players, just return
        if (optionalProfile.isEmpty() && playerTypeRequiresLogin) {
            return;
        }
        // The new profile will only be created for online players
        Profile profile = optionalProfile.orElse(new Profile(player.getUniqueId(), player.getName()));

        // Check if profile is logged in and set its join date
        if (!playerTypeRequiresLogin || LoginUtil.isPlayerLoggedIn(plugin, profile, player)) {
            profile.setJoinDate(player.getName(), LoginUtil.getInetAddress(player.getSocketAddress()).orElse(null));
            plugin.getDatabase().saveProfileCatching(profile);
        }
    }

    @EventHandler
    public void updateLastServer(ServerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
        if (optionalProfile.isEmpty()) {
            return;
        }
        Profile profile = optionalProfile.get();

        // Check if profile is logged in and set its join date
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin() ||
            LoginUtil.isPlayerLoggedIn(plugin, profile, player)) {
            profile.setLastServerName(event.getTarget().getName());
            plugin.getDatabase().saveProfileCatching(profile);
            // Store last server information on BungeeCord's side
            if (event.getPlayer().hasPermission(Permissions.REDIRECT_TO_LAST_SERVER)) {
                event.getPlayer().setReconnectServer(event.getTarget());
            } else {
                event.getPlayer().setReconnectServer(null);
            }
        }
    }

    @EventHandler
    public void updateQuitDate(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
        if (optionalProfile.isEmpty()) {
            return;
        }
        Profile profile = optionalProfile.get();

        // Check if profile is logged in and set its join date
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin() ||
            LoginUtil.isPlayerLoggedIn(plugin, profile, player)) {
            profile.setQuitDate();
            plugin.getDatabase().saveProfileCatching(profile);
        }
    }
}
