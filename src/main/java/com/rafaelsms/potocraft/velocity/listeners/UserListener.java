package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUserManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserListener {

    private final @NotNull VelocityPlugin plugin;
    private final @NotNull VelocityUserManager userManager;

    public UserListener(@NotNull VelocityPlugin plugin, @NotNull VelocityUserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;
    }

    @Subscribe
    private void onSuccessfulJoin(PostLoginEvent event) {
        try {
            userManager.userJoinedListener(event.getPlayer().getUniqueId(), event.getPlayer());
        } catch (Exception ignored) {
            event.getPlayer().disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
        }
    }

    @Subscribe
    private void onQuit(DisconnectEvent event) {
        userManager.userQuitListener(event.getPlayer().getUniqueId());
    }

}
