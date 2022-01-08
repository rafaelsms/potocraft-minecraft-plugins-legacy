package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.Permissions;
import com.rafaelsms.potocraft.util.Location;
import com.rafaelsms.potocraft.util.PlayerType;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RegisterCommand implements RawCommand {

    private final VelocityPlugin plugin;

    public RegisterCommand(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Prevent console from executing
        if (!(invocation.source() instanceof Player player)) {
            // TODO allow console to register players?
            invocation.source().sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return;
        }

        plugin.retrievePlayerType(player.getUniqueId(), playerType -> {
            // Ignore online/floodgate players
            if (playerType != PlayerType.OFFLINE_PLAYER) {
                player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
                return;
            }

            // Retrieve profile
            plugin.getDatabase().retrieveProfile(player.getUniqueId(), profile -> {
                if (profile.hasPin()) {
                    if (profile.isLoggedIn(player.getRemoteAddress())) {
                        // TODO already has, suggest /changepin
                    } else {
                        // TODO use /login
                    }
                    return;
                }

                // Retrieve arguments
                String[] arguments = Util.parseArguments(invocation.arguments());
                if (arguments.length != 2) {
                    // TODO help
                    return;
                }

                // Parse to PIN
                Optional<Integer> pinOptional = Util.parsePin(arguments[0]);
                Optional<Integer> pinConfirmationOptional = Util.parsePin(arguments[1]);
                if (pinOptional.isEmpty() || pinConfirmationOptional.isEmpty()) {
                    // TODO invalid pin
                    return;
                }

                // Check that PINs are equal
                int pin = pinOptional.get();
                int pinConfirmation = pinConfirmationOptional.get();
                if (pin != pinConfirmation) {
                    // TODO pins dont match
                    return;
                }

                // Update profile with new PIN
                if (!profile.setPIN(pin)) {
                    // TODO failed format
                    return;
                }

                // Set as logged in
                Optional<Location> lastLocation = profile.getLastLocation();
                profile.setLoggedIn(player.getRemoteAddress());
                // Save status
                plugin.getDatabase().saveProfile(profile, () -> {
                    // Attempt to move player to new server
                    String lobbyServerName = plugin.getSettings().getLobbyServer();
                    Optional<RegisteredServer> lobbyOptional = plugin.getProxyServer().getServer(lobbyServerName);
                    if (lastLocation.isPresent()) {
                        String serverName = lastLocation.get().getServerName();
                        Optional<RegisteredServer> optionalServer = plugin.getProxyServer().getServer(serverName);
                        sendPlayerToServer(player, optionalServer.orElse(lobbyOptional.orElse(null)));
                    } else {
                        sendPlayerToServer(player, lobbyOptional.orElse(null));
                    }
                }, exception -> player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile()));
            }, () -> player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile()), exception -> player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile()));
        }, () -> player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType()), exception -> player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType()));
    }

    private void sendPlayerToServer(@NotNull Player player, @Nullable RegisteredServer server) {
        Component serverUnavailable = plugin.getSettings().getKickMessageTransferServerUnavailable();
        if (server == null) {
            if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                player.disconnect(serverUnavailable);
            } else {
                player.sendMessage(serverUnavailable);
            }
        }
        player.createConnectionRequest(server).connect().whenComplete((result, throwable) -> {
            if ((result != null && !result.isSuccessful()) || throwable != null) {
                if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                    player.disconnect(serverUnavailable);
                } else {
                    player.sendMessage(serverUnavailable);
                }
            }
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true || invocation.source().hasPermission(Permissions.REGISTER_COMMAND);
    }
}
