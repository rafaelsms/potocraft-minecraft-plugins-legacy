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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class LoginCommand implements RawCommand {

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

        // Check if required arguments are present and nothing else
        List<String> arguments = TextUtil.parseArguments(invocation.arguments());
        if (arguments.size() != 1) {
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
        Optional<Integer> pinOptional = TextUtil.parsePin(arguments.get(0));
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
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> arguments = TextUtil.parseArguments(invocation.arguments());
        if (arguments.size() > 1) {
            return List.of();
        }
        return List.of("123456", "000000");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_LOGIN);
    }
}
