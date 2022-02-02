package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * We will: - prevent offline players which are not logged in from joining a server different from the login server; -
 * redirect online/logged in players to their last server.
 * <p>
 * In these events, setting the server to null doesn't disconnect the player, instead keep they in a limbo.
 */
public class RedirectPlayerListener {

    private final @NotNull LoginManagerPlugin plugin;

    public RedirectPlayerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void redirectLoggedOffPlayersToLogin(ServerPreConnectEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        // Just allow if player type doesn't require login
        if (!PlayerType.get(player).requiresLogin()) {
            continuation.resume();
            return;
        }

        // If it requires login, we need to check if player is logged in before allowing the connection
        Optional<Profile> profileOptional;
        try {
            profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
        } catch (Database.DatabaseException ignored) {
            // Failed to retrieve profile, disconnect
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            continuation.resume();
            return;
        }

        // Check if player is logged off
        if ((profileOptional.isEmpty() || !Util.isPlayerLoggedIn(plugin, profileOptional.get(), player))) {
            Optional<RegisteredServer> loginServer = getLoginServer();
            if (loginServer.isPresent()) {
                // Send to login server
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(loginServer.get()));
            } else {
                // Disconnect player as login server is unavailable
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(plugin.getConfiguration().getKickMessageLoginServerUnavailable());
            }
        }
        continuation.resume();
    }

    @Subscribe
    private void printPlayerType(PostLoginEvent event) {
        plugin.getLogger()
              .info("Player {} (uuid = {}) connection type is {}",
                    event.getPlayer().getUsername(),
                    event.getPlayer().getUniqueId(),
                    PlayerType.get(event.getPlayer()));
    }

    @Subscribe
    private void redirectToLastServer(PlayerChooseInitialServerEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        // Check if player has permission to be redirected
        if (!player.hasPermission(Permissions.REDIRECT_TO_LAST_SERVER)) {
            continuation.resume();
            return;
        }

        // Get player profile
        Optional<Profile> profileOptional;
        try {
            profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
        } catch (Database.DatabaseException ignored) {
            event.setInitialServer(null);
            event.getPlayer().disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            continuation.resume();
            return;
        }

        // Redirect to last server if present
        Optional<RegisteredServer> lastServerOptional = Optional.empty();
        if (profileOptional.isPresent()) {
            Profile profile = profileOptional.get();
            Optional<String> lastServerNameOptional = profile.getLastServerName();
            if (lastServerNameOptional.isPresent()) {
                lastServerOptional = getServer(lastServerNameOptional.get());
            }
        }

        // Redirect to last server
        lastServerOptional.ifPresent(event::setInitialServer);
        continuation.resume();
    }

    private @NotNull Optional<RegisteredServer> getLoginServer() {
        return getServer(plugin.getConfiguration().getLoginServer());
    }

    private @NotNull Optional<RegisteredServer> getServer(@Nullable String serverName) {
        return plugin.getServer().getServer(serverName);
    }
}
