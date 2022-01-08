package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.Permissions;
import com.rafaelsms.potocraft.util.Location;
import com.rafaelsms.potocraft.util.PlayerType;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
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
        plugin.retrievePlayerType(player.getUniqueId(), playerType -> {
            // Ignore player if it has online mode
            if (playerType != PlayerType.OFFLINE_PLAYER) {
                player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
                return;
            }

            // Retrieve profile and handle on the handler
            plugin.getDatabase().retrieveProfile(
                    player.getUniqueId(),
                    new LoginCommandHandler(player, Util.parseArguments(invocation.arguments())),
                    () -> player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile()),
                    exception -> player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile()));
        }, () -> player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType()), exception -> player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType()));
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

    private class LoginCommandHandler implements Consumer<VelocityUser> {

        private final Player player;
        private final String[] arguments;

        private LoginCommandHandler(@NotNull Player player, @NotNull String[] arguments) {
            this.player = player;
            this.arguments = arguments;
        }

        @Override
        public void accept(VelocityUser profile) {
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
            plugin.getDatabase().saveProfile(profile, () -> {
                // If saved successfully, send message
                player.sendMessage(plugin.getSettings().getCommandLoggedIn());

                // Transfer player to last server it was in if it exists or to the lobby server it exists
                Optional<RegisteredServer> lobbyServerOptional = plugin.getProxyServer().getServer(plugin.getSettings().getLobbyServer());
                if (lastLocation.isPresent()) {
                    String lastServerName = lastLocation.get().getServerName();
                    Optional<RegisteredServer> serverOptional = plugin.getProxyServer().getServer(lastServerName);
                    sendToServer(serverOptional.orElse(lobbyServerOptional.orElse(null)));
                } else {
                    sendToServer(lobbyServerOptional.orElse(null));
                }
            }, exception -> player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile()));
        }

        private void sendToServer(@Nullable RegisteredServer server) {
            if (server == null) {
                Component reason = plugin.getSettings().getKickMessageTransferServerUnavailable();
                if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                    player.disconnect(reason);
                } else {
                    player.sendMessage(reason);
                }
            }
            player.createConnectionRequest(server).connect().whenComplete((result, throwable) -> {
                if (throwable != null || (result != null && !result.isSuccessful())) {
                    Component reason = plugin.getSettings().getKickMessageTransferServerUnavailable();
                    if (plugin.getSettings().isKickIfLobbyUnavailable()) {
                        player.disconnect(reason);
                    } else {
                        player.sendMessage(reason);
                    }
                }
            });
        }
    }
}
