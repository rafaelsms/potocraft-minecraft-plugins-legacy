package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

/**
 * We will: - allow offline players to join if their name is not used by Mojang; - allow online and floodgate players to
 * join normally.
 */
public class OfflineCheckerListener implements Listener {

    private final @NotNull LoginManagerPlugin plugin;

    public OfflineCheckerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void denyJavaWithBedrockPrefix(PreLoginEvent event) {
        // Ignore cancelled events already
        if (event.isCancelled()) {
            return;
        }

        event.registerIntent(plugin);
        // If is a floodgate player, allow without checking
        FloodgateApi floodgateApi = FloodgateApi.getInstance();
        if (event.getConnection().getUniqueId() != null) {
            FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(event.getConnection().getUniqueId());
            if (floodgatePlayer != null) {
                event.completeIntent(plugin);
                return;
            }
        }

        // If is not a floodgate player and is using Floodgate's prefix, deny login
        if (floodgateApi.getPlayerPrefix().length() > 0 &&
            event.getConnection().getName().startsWith(floodgateApi.getPlayerPrefix())) {
            event.setCancelled(true);
            event.setCancelReason(plugin.getConfiguration().getKickMessageInvalidPrefixForJavaPlayer());
            event.completeIntent(plugin);
            return;
        }

        // Validate username against configuration regex for Java players
        if (!plugin.getConfiguration()
                   .getAllowedJavaUsernamesRegex()
                   .matcher(event.getConnection().getName())
                   .matches()) {
            event.setCancelled(true);
            event.setCancelReason(plugin.getConfiguration().getKickMessageInvalidJavaUsername());
        }
        event.completeIntent(plugin);
    }


    @EventHandler(priority = EventPriority.LOW) // Ensure we are after Floodgate's LOWEST
    public void acceptAndVerifyOfflinePlayers(PreLoginEvent event) {
        // Ignore cancelled events already
        if (event.isCancelled()) {
            return;
        }
        event.registerIntent(plugin);

        // Allow floodgate player
        if (event.getConnection().getUniqueId() != null &&
            FloodgateApi.getInstance().getPlayer(event.getConnection().getUniqueId()) != null) {
            event.completeIntent(plugin);
            return;
        }

        plugin.runAsync(() -> {
            // Check if player has existing account
            try {
                // If Mojang player exists, force online mode so the account is secured against offline players
                // If it doesn't exist, force offline mode, so we can require authentication
                event.getConnection().setOnlineMode(isMojangUsernameExistent(event.getConnection().getName()));
            } catch (IOException exception) {
                // If we failed to request, disconnect player
                plugin.logger().warn("Failed to access Mojang's username API: {}", exception.getMessage());
                event.setCancelReason(plugin.getConfiguration().getKickMessageCouldNotCheckMojangUsername());
                event.setCancelled(true);
                exception.printStackTrace();
            }
            event.completeIntent(plugin);
        });
    }

    private boolean isMojangUsernameExistent(@NotNull String name) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/%s".formatted(name));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection.getResponseCode() == 200;
    }
}
