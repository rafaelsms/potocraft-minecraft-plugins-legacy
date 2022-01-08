package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.Permissions;
import com.rafaelsms.potocraft.util.Location;
import com.rafaelsms.potocraft.util.PlayerType;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.command.CommandSource;
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

public class LoginCommand implements RawCommand {

    private final VelocityPlugin plugin;

    private static final Predicate<String> unfinishedPinPattern = Pattern.compile("^[0-9]{1,6}$").asMatchPredicate();

    public LoginCommand(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return;
        }

        // Check player connection type, as online players should not be able to use this command
        Optional<PlayerType> playerTypeOptional = plugin.getPlayerType(player);
        if (playerTypeOptional.isEmpty()) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType());
            plugin.logger().warn("Failed to retrieve player connection type for player (%s, uuid = %s) in login command"
                    .formatted(player.getUsername(), player.getUniqueId().toString()));
            return;
        }
        PlayerType playerType = playerTypeOptional.get();

        // Ignore player if it has online mode
        if (playerType != PlayerType.OFFLINE_PLAYER) {
            player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
            return;
        }

        // Retrieve profile and handle on the handler
        plugin.getDatabase().getProfile(player.getUniqueId()).whenComplete((profile, retrievalThrowable) -> {
            if (retrievalThrowable != null) {
                plugin.logger().warn("Failed to retrieve player (%s, uuid = %s) profile for login: %s"
                        .formatted(player.getUsername(), player.getUniqueId().toString(), retrievalThrowable.getLocalizedMessage()));
                player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
            } else if (profile == null) {
                player.sendMessage(plugin.getSettings().getCommandMustRegisterFirst());
            } else {

                // Does not have a PIN => warn to register
                if (!profile.hasPin()) {
                    player.sendMessage(plugin.getSettings().getCommandMustRegisterFirst());
                    return;
                }

                // Already logged in => warn player
                if (profile.isLoggedIn(player.getRemoteAddress())) {
                    Component reason = plugin.getSettings().getCommandAlreadyLoggedIn();
                    if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                        player.disconnect(reason);
                    } else {
                        player.sendMessage(reason);
                    }
                    return;
                }

                // Check if required arguments are present and nothing else
                String[] arguments = Util.parseArguments(invocation.arguments());
                if (arguments.length != 1) {
                    player.sendMessage(plugin.getSettings().getCommandLoginHelp());
                    return;
                }

                // Attempt PIN format match
                Optional<Integer> pinOptional = Util.parsePin(arguments[0]);
                if (pinOptional.isEmpty()) {
                    player.sendMessage(plugin.getSettings().getCommandLoginHelp());
                    return;
                }

                // Invalid PIN => Disconnect
                int pin = pinOptional.get();
                if (!profile.isValidPIN(pin)) {
                    player.disconnect(plugin.getSettings().getCommandIncorrectPIN());
                    return;
                }

                Optional<Location> lastLocation = profile.getLastLocation();
                // Set as logged in and save
                profile.setLoggedIn(player.getRemoteAddress());
                plugin.getDatabase().saveProfile(profile).whenComplete((unused, saveThrowable) -> {
                    if (saveThrowable != null) {
                        player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile());
                        plugin.logger().warn("Couldn't save player (%s, uuid = %s) profile after logging in: %s"
                                .formatted(player.getUsername(), player.getUniqueId().toString(), saveThrowable.getLocalizedMessage()));
                    } else {
                        // If saved successfully, send message
                        player.sendMessage(plugin.getSettings().getCommandLoggedIn());

                        // Transfer player to last server it was in if it exists or to the lobby server it exists
                        Optional<RegisteredServer> lobbyServerOptional = plugin.getProxyServer().getServer(plugin.getSettings().getLobbyServer());
                        if (lastLocation.isPresent()) {
                            String lastServerName = lastLocation.get().getServerName();
                            Optional<RegisteredServer> serverOptional = plugin.getProxyServer().getServer(lastServerName);
                            sendToServer(player, serverOptional.orElse(lobbyServerOptional.orElse(null)));
                        } else {
                            sendToServer(player, lobbyServerOptional.orElse(null));
                        }
                    }
                });
            }
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] arguments = Util.parseArguments(invocation.arguments());
        if (arguments.length > 1) return List.of();
        if (unfinishedPinPattern.test(arguments[0])) {
            return List.of("123456");
        } else {
            return List.of();
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.LOGIN_COMMAND);
    }

    private void sendToServer(@NotNull Player player, @Nullable RegisteredServer server) {
        if (server == null) {
            handleLobbyUnavailable(player);
            return;
        }
        player.createConnectionRequest(server).connect().whenComplete((result, throwable) -> {
            if (throwable != null || (result != null && !result.isSuccessful())) {
                handleLobbyUnavailable(player);
            }
        });
    }

    private void handleLobbyUnavailable(@NotNull Player player) {
        Component reason = plugin.getSettings().getKickMessageTransferServerUnavailable();
        if (plugin.getSettings().isKickIfLobbyUnavailable()) {
            player.disconnect(reason);
        } else {
            player.sendMessage(reason);
        }
    }
}
