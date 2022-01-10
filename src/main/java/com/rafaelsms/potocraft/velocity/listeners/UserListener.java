package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.velocity.user.VelocityUserManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserListener {

    private final @NotNull VelocityUserManager userManager;

    public UserListener(@NotNull VelocityUserManager userManager) {
        this.userManager = userManager;
    }

    @Subscribe
    private void onSuccessfulJoin(PostLoginEvent event) {
        userManager.userJoinedListener(event.getPlayer().getUniqueId(), event.getPlayer());
    }

    @Subscribe
    private void onQuit(DisconnectEvent event) {
        userManager.userQuitListener(event.getPlayer().getUniqueId());
    }

}
