package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ChangePinCommand implements RawCommand {

    // /<command> <old pin> <new pin> <new pin>

    private final @NotNull LoginManagerPlugin plugin;

    public ChangePinCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Check if source is a player
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Check player type
        if (!PlayerType.get(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        // Check arguments
        List<String> arguments = TextUtil.parseArguments(invocation.arguments());
        if (arguments.size() != 3) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinHelp());
            return;
        }

        // Parse old pin
        Optional<Integer> oldPinOptional = TextUtil.parsePin(arguments.get(0));
        if (oldPinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinHelp());
            return;
        }
        int oldPin = oldPinOptional.get();

        // Get new pin and verify they are equal
        Optional<Integer> pinOptional = TextUtil.parseMatchingPins(arguments.get(1), arguments.get(2));
        if (pinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinInvalidPins());
            return;
        }
        int newPin = pinOptional.get();

        // Retrieve player profile
        Profile profile;
        try {
            Optional<Profile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            if (profileOptional.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getCommandChangePinRegisterFirst());
                return;
            }
            profile = profileOptional.get();

            // Check if player is online
            if (!Util.isPlayerLoggedIn(plugin, profile, player)) {
                player.sendMessage(plugin.getConfiguration().getCommandLoggedInOnly());
                return;
            }
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Check if old pin is valid
        if (!profile.isPinValid(oldPin)) {
            player.disconnect(plugin.getConfiguration().getCommandIncorrectPin());
            return;
        }

        // Check if couldn't set pin
        if (!profile.setPin(newPin)) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPinFormat());
            return;
        }

        // Update player's pin
        try {
            plugin.getDatabase().saveProfile(profile);
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (TextUtil.parseArguments(invocation.arguments()).size() <= 2) {
            return List.of("123456", "000000");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_CHANGE_PIN);
    }
}
