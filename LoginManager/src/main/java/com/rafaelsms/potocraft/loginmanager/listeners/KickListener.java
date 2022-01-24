package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class KickListener {

    private final @NotNull LoginManagerPlugin plugin;

    public KickListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void kickOnKick(KickedFromServerEvent event) {
        Component kickedMessage = event.getServerKickReason().orElse(plugin.getConfiguration().getKickMessageGeneric());
        event.setResult(KickedFromServerEvent.DisconnectPlayer.create(kickedMessage));
    }
}
