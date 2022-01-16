package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * We will:
 * - check if offline profiles are still logged in;
 * - update offline profiles' login date if needed;
 * - update offline profiles' logged in status if needed;
 * - validate profiles' logged in status on startup;
 * - update join/quit date for online players.
 */
public class ProfileUpdater {

    private final @NotNull LoginManagerPlugin plugin;

    public ProfileUpdater(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void updateJoinDate(PostLoginEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            Player player = event.getPlayer();
            Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
            if (optionalProfile.isEmpty()) {
                continuation.resume();
                return;
            }
            Profile profile = optionalProfile.get();

            // Check if profile is logged in and set its join date
            if (!PlayerType.get(player).requiresLogin() || Util.isPlayerLoggedIn(plugin, profile, player)) {
                profile.setJoinDate();
                plugin.getDatabase().saveProfileCatching(profile);
            }
            continuation.resume();
        });
    }

    @Subscribe
    private void updateQuitDate(DisconnectEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            Player player = event.getPlayer();
            Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
            if (optionalProfile.isEmpty()) {
                continuation.resume();
                return;
            }
            Profile profile = optionalProfile.get();

            // Check if profile is logged in and set its join date
            if (!PlayerType.get(player).requiresLogin() || Util.isPlayerLoggedIn(plugin, profile, player)) {
                profile.setQuitDate();
                plugin.getDatabase().saveProfileCatching(profile);
            }
            continuation.resume();
        });
    }
}
