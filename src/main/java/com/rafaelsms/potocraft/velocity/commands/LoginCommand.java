package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.util.DatabaseException;
import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.common.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.rafaelsms.potocraft.velocity.util.VelocityUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class LoginCommand implements RawCommand {

    private final @NotNull VelocityPlugin plugin;

    private static final Predicate<String> unfinishedPinPattern = Pattern.compile("^[0-9]{1,6}$").asMatchPredicate();

    public LoginCommand(@NotNull VelocityPlugin plugin) {
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
        PlayerType playerType;
        try {
            playerType = plugin.getPlayerType(player);
        } catch (Exception ignored) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType());
            plugin
                    .logger()
                    .warn("Failed to retrieve player connection type for player (%s, uuid = %s) in login command".formatted(
                            player.getUsername(),
                            player.getUniqueId().toString()));
            return;
        }

        // Ignore player if it has online mode
        if (playerType != PlayerType.OFFLINE_PLAYER) {
            player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
            return;
        }

        // Retrieve profile and handle on the handler
        VelocityProfile profile;
        try {
            Optional<VelocityProfile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            // If no profile, must register
            if (profileOptional.isEmpty()) {
                player.sendMessage(plugin.getSettings().getCommandMustRegisterFirst());
                return;
            }

            profile = profileOptional.get();
        } catch (DatabaseException ignored) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
            return;
        }

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

        // Set as logged in and save
        profile.setLoggedIn(player.getRemoteAddress());
        try {
            plugin.getDatabase().saveProfile(profile);
        } catch (Exception ignored) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile());
            return;
        }

        // If saved successfully, send message
        player.sendMessage(plugin.getSettings().getCommandLoggedIn());

        // Transfer player to last server it was in if it exists or to the lobby server it exists
        VelocityUtil.sendPlayerToLastServer(plugin, player, profile);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] arguments = Util.parseArguments(invocation.arguments());
        if (arguments.length > 1) {
            return List.of();
        }
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
}
