package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.util.Location;
import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.common.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RegisterCommand implements RawCommand {

    // This should match white typing the first pin and after successfully typing the first pin, while typing the confirmation one
    private static final Predicate<String> pinSuggestion = Pattern.compile("^\\h*(\\d{1,6})$|((\\d{6})\\h*(\\d{1,6})$)").asPredicate();

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

        Optional<PlayerType> playerTypeOptional = plugin.getPlayerType(player);
        if (playerTypeOptional.isEmpty()) {
            plugin.logger().warn("Failed to retrieve player connection type for player (%s, uuid = %s) when registering"
                    .formatted(player.getUsername(), player.getUniqueId().toString()));
            player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType());
            return;
        }
        PlayerType playerType = playerTypeOptional.get();

        // Ignore online/floodgate players
        if (playerType != PlayerType.OFFLINE_PLAYER) {
            player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
            return;
        }

        // Retrieve profile
        plugin.getDatabase().getProfile(player.getUniqueId()).whenComplete((_profile, retrievalThrowable) -> {
            if (retrievalThrowable != null) {
                plugin.logger().warn("Failed to retrieve player (%s, uuid = %s) profile for registering: %s"
                        .formatted(player.getUsername(), player.getUniqueId().toString(), retrievalThrowable.getLocalizedMessage()));
                player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
                return;
            }
            // Get given profile
            VelocityProfile profile = _profile;
            if (profile == null) {
                profile = new VelocityProfile(plugin, player.getUniqueId(), player.getUsername());
            }

            // Check if profile already has a PIN
            if (profile.hasPin()) {
                if (profile.isLoggedIn(player.getRemoteAddress()) &&
                            invocation.source().hasPermission(Permissions.CHANGE_PIN_COMMAND)) {
                    player.sendMessage(plugin.getSettings().getCommandRegisterShouldChangePinInstead());
                } else {
                    player.sendMessage(plugin.getSettings().getCommandRegisterShouldLoginInstead());
                }
                return;
            }

            // Retrieve arguments
            String[] arguments = Util.parseArguments(invocation.arguments());
            if (arguments.length != 2) {
                player.sendMessage(plugin.getSettings().getCommandRegisterHelp());
                return;
            }

            // Parse to PIN
            Optional<Integer> pinOptional = Util.parsePin(arguments[0]);
            Optional<Integer> pinConfirmationOptional = Util.parsePin(arguments[1]);
            if (pinOptional.isEmpty() || pinConfirmationOptional.isEmpty()) {
                player.sendMessage(plugin.getSettings().getCommandRegisterInvalidPins());
                return;
            }

            // Check that PINs are equal
            int pin = pinOptional.get();
            int pinConfirmation = pinConfirmationOptional.get();
            if (pin != pinConfirmation) {
                player.sendMessage(plugin.getSettings().getCommandRegisterPinsDoNotMatch());
                return;
            }

            // Update profile with new PIN
            if (!profile.setPIN(pin)) {
                player.sendMessage(plugin.getSettings().getCommandRegisterFormattingFailed());
                return;
            }

            // Set as logged in
            Optional<Location> lastLocation = profile.getLastLocation();
            profile.setLoggedIn(player.getRemoteAddress());
            // Save status
            plugin.getDatabase().saveProfile(profile).whenComplete((unused, saveThrowable) -> {
                if (saveThrowable != null) {
                    plugin.logger().warn("Failed to save profile for player %s (uuid = %s) on register: %s"
                            .formatted(player.getUsername(), player.getUniqueId().toString(), saveThrowable.getLocalizedMessage()));
                    player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile());
                } else {
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
                }
            });
        });
    }

    private void sendPlayerToServer(@NotNull Player player, @Nullable RegisteredServer server) {
        Component serverUnavailable = plugin.getSettings().getKickMessageTransferServerUnavailable();
        if (server == null) {
            if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                player.disconnect(serverUnavailable);
            } else {
                player.sendMessage(serverUnavailable);
            }
            plugin.logger().warn("Server was not found, couldn't move player %s (uuid = %s) after registering"
                    .formatted(player.getUsername(), player.getUniqueId().toString()));
            return;
        }
        player.createConnectionRequest(server).connect().whenComplete((result, throwable) -> {
            if ((result != null && !result.isSuccessful()) || throwable != null) {
                if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                    player.disconnect(serverUnavailable);
                } else {
                    player.sendMessage(serverUnavailable);
                }
                plugin.logger().warn("Server %s is unavailable, couldn't move player %s (uuid = %s) after registering"
                        .formatted(server.getServerInfo().getName(), player.getUsername(), player.getUniqueId().toString()));
            }
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] arguments = Util.parseArguments(invocation.arguments());
        if (arguments.length > 2) return List.of();
        if (pinSuggestion.test(invocation.arguments())) {
            return List.of("123456");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true || invocation.source().hasPermission(Permissions.REGISTER_COMMAND);
    }
}
