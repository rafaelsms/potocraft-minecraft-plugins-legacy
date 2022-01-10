package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ProfileUpdater {

    private final @NotNull VelocityPlugin plugin;

    public ProfileUpdater(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    private void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        // Retrieve player profile
        PlayerType playerType;
        try {
            playerType = plugin.getPlayerType(player);
        } catch (Exception ignored) {
            Component reason = plugin.getSettings().getKickMessageCouldNotCheckPlayerType();
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
            continuation.resume();
            return;
        }

        // Asynchronously handle database
        CompletableFuture.runAsync(() -> {
            Optional<VelocityProfile> profileOptional;
            try {
                profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            } catch (Exception ignored) {
                Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                continuation.resume();
                return;
            }

            if (profileOptional.isEmpty()) {
                // Offline players have their profile created on /login or /register commands
                if (playerType != PlayerType.OFFLINE_PLAYER) {
                    VelocityProfile profile = new VelocityProfile(plugin, player.getUniqueId(), player.getUsername());
                    try {
                        plugin.getDatabase().saveProfile(profile);
                    } catch (Exception ignored) {
                        Component reason = plugin.getSettings().getKickMessageCouldNotSaveProfile();
                        event.setResult(ResultedEvent.ComponentResult.denied(reason));
                    }
                    continuation.resume();
                    return;
                }

                // offline players just continue
                continuation.resume();
            } else {
                VelocityProfile profile = profileOptional.get();

                // Offline players have their profile updated on /login or /register commands
                if (playerType != PlayerType.OFFLINE_PLAYER) {
                    profile.updateLastPlayerName(event.getPlayer().getUsername());
                    profile.updateJoinDate();
                    try {
                        plugin.getDatabase().saveProfile(profile);
                    } catch (Exception ignored) {
                        Component reason = plugin.getSettings().getKickMessageCouldNotSaveProfile();
                        event.setResult(ResultedEvent.ComponentResult.denied(reason));
                    }
                    continuation.resume();
                    return;
                }

                // offline players just continue
                continuation.resume();
            }
        });
    }

    @Subscribe
    private void onDisconnect(DisconnectEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        // Asynchronously handle database
        CompletableFuture.runAsync(() -> {
            try {
                PlayerType playerType = plugin.getPlayerType(player);
                Optional<VelocityProfile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
                if (profileOptional.isEmpty()) {
                    continuation.resume();
                    return;
                }

                // Offline players must be logged in to set quit date
                VelocityProfile profile = profileOptional.get();
                if (playerType != PlayerType.OFFLINE_PLAYER ||
                    profile.isLoggedIn(event.getPlayer().getRemoteAddress())) {
                    profile.updateQuitDate();
                    plugin.getDatabase().saveProfile(profile);
                }
            } catch (Exception ignored) {
            }
            continuation.resume();
        });
    }
}
