package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginCommand implements RawCommand {

    private final Pattern pinExtractor = Pattern.compile("^\\s*(\\d{6})(\\s+.*)?$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public LoginCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Check player connection type, as online players should not be able to use this command
        if (!PlayerType.get(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        Matcher matcher = pinExtractor.matcher(invocation.arguments());
        if (!matcher.matches()) {
            player.sendMessage(plugin.getConfiguration().getCommandLoginHelp());
            return;
        }

        // Retrieve profile and handle on the handler
        Profile profile;
        try {
            Optional<Profile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            // If no profile, must register
            if (profileOptional.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getCommandLoginRegisterFirst());
                return;
            }
            profile = profileOptional.get();

            // Check if is online
            if (Util.isPlayerLoggedIn(plugin, profile, player)) {
                player.sendMessage(plugin.getConfiguration().getCommandLoginAlreadyLoggedIn());
                return;
            }
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Does not have a PIN => warn to register
        if (!profile.hasPin()) {
            player.sendMessage(plugin.getConfiguration().getCommandLoginRegisterFirst());
            return;
        }

        // Attempt PIN format match
        Optional<Integer> pinOptional = TextUtil.parsePin(matcher.group(1));
        if (pinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandLoginHelp());
            return;
        }

        // Check if couldn't set pin
        if (!profile.setPin(pinOptional.get())) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPinFormat());
            return;
        }

        try {
            // Set as logged in and save
            profile.setLoggedIn(player.getRemoteAddress());
            player.sendMessage(plugin.getConfiguration().getCommandLoggedIn());
            plugin.getDatabase().saveProfile(profile);
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }

        // Attempt to send player to lobby
        if (player.hasPermission(Permissions.REDIRECT_TO_LAST_SERVER) && profile.getLastServerName().isPresent()) {
            Optional<RegisteredServer> serverOptional = plugin.getServer().getServer(profile.getLastServerName().get());
            serverOptional.ifPresent(registeredServer -> player
                    .createConnectionRequest(registeredServer)
                    .connectWithIndication()
                    .whenComplete((success, throwable) -> {
                        if (throwable != null || success == null || !success) {
                            Util.sendPlayerToDefault(plugin, player);
                        }
                    }));
            return;
        }
        Util.sendPlayerToDefault(plugin, player);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of("123456", "000000");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_LOGIN);
    }
}
