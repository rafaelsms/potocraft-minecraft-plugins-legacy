package com.rafaelsms.potocraft.velocity.util;

import com.rafaelsms.potocraft.common.util.Location;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class VelocityUtil {

    private VelocityUtil() {
    }

    public static void sendPlayerToLastServer(@NotNull VelocityPlugin plugin, @NotNull Player player,
                                              @NotNull VelocityProfile profile) {
        sendPlayerToServer(plugin, player, getLastServer(plugin, profile));
    }

    public static void sendPlayerToServer(@NotNull VelocityPlugin plugin, @NotNull Player player, @Nullable RegisteredServer server) {
        try {
            if (server == null)
                throw new NullPointerException();
            if (!player.createConnectionRequest(server).connect().get().isSuccessful())
                throw new IllegalStateException();
        } catch (Exception ignored) {
            Component reason = plugin.getSettings().getKickMessageTransferServerUnavailable();
            if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                player.disconnect(reason);
            } else {
                player.sendMessage(reason);
            }
        }
    }

    public static @Nullable RegisteredServer getLastServer(@NotNull VelocityPlugin plugin, @NotNull VelocityProfile profile) {
        String lobbyServerName = plugin.getSettings().getLobbyServer();
        RegisteredServer server = plugin.getProxyServer().getServer(lobbyServerName).orElse(null);

        // Overwrite with last server
        Optional<Location> optionalLocation = profile.getLastLocation();
        if (optionalLocation.isPresent()) {
            String serverName = optionalLocation.get().getServerName();
            server = plugin.getProxyServer().getServer(serverName).orElse(server);
        }

        return server;
    }

}
