package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.LoginUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * We will: - prevent offline players which are not logged in from joining a server different from the login server.
 * <p>
 * In these events, setting the server to null doesn't disconnect the player, instead keep they in a limbo.
 */
public class RedirectPlayerListener implements Listener {

    private final @NotNull LoginManagerPlugin plugin;

    public RedirectPlayerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void redirectLoggedOffPlayersToLogin(ServerConnectEvent event) {
        // Just allow if player type doesn't require login
        ProxiedPlayer player = event.getPlayer();
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin()) {
            return;
        }

        // If it requires login, we need to check if player is logged in before allowing the connection
        Optional<Profile> profileOptional;
        try {
            profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
        } catch (DatabaseException ignored) {
            // Failed to retrieve profile, disconnect
            event.setCancelled(true);
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Check if player is logged off
        if ((profileOptional.isEmpty() || !LoginUtil.isPlayerLoggedIn(plugin, profileOptional.get(), player))) {
            Optional<ServerInfo> loginServer = getLoginServer();
            if (loginServer.isPresent()) {
                // Send to login server
                event.setTarget(loginServer.get());
            } else {
                // Disconnect player as login server is unavailable
                event.setCancelled(true);
                event.getPlayer().disconnect(plugin.getConfiguration().getKickMessageLoginServerUnavailable());
            }
        }
    }

    @EventHandler
    public void resetPlayersReconnectServer(ServerConnectEvent event) {
        // No matter player's permission, we reset reconnect server so player can rejoin if his last server is offline
        event.getPlayer().setReconnectServer(null);
    }

    private @NotNull Optional<ServerInfo> getLoginServer() {
        return getServer(plugin.getConfiguration().getLoginServer());
    }

    private @NotNull Optional<ServerInfo> getServer(@Nullable String serverName) {
        return Optional.ofNullable(plugin.getProxy().getServerInfo(serverName));
    }
}
