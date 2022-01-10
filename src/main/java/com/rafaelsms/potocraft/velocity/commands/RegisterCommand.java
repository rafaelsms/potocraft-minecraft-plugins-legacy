package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.common.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.rafaelsms.potocraft.velocity.util.VelocityUtil;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RegisterCommand implements RawCommand {

    // This should match white typing the first pin and after successfully typing the first pin, while typing the confirmation one
    private static final Predicate<String> pinSuggestion =
            Pattern.compile("^\\h*(\\d{1,6})$|((\\d{6})\\h*(\\d{1,6})$)").asPredicate();

    private final @NotNull VelocityPlugin plugin;

    public RegisterCommand(@NotNull VelocityPlugin plugin) {
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

        PlayerType playerType;
        try {
            playerType = plugin.getPlayerType(player);
        } catch (Exception ignored) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType());
            return;
        }

        // Ignore online/floodgate players
        if (playerType != PlayerType.OFFLINE_PLAYER) {
            player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
            return;
        }

        // Retrieve profile
        VelocityProfile profile;
        try {
            profile = plugin
                    .getDatabase()
                    .getProfile(player.getUniqueId())
                    .orElse(new VelocityProfile(plugin, player.getUniqueId(), player.getUsername()));
        } catch (Exception ignored) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
            return;
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
        profile.setLoggedIn(player.getRemoteAddress());
        // Save status
        try {
            plugin.getDatabase().saveProfile(profile);
        } catch (Exception ignored) {
            player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile());
            return;
        }

        // Attempt to move player to last server
        VelocityUtil.sendPlayerToLastServer(plugin, player, profile);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] arguments = Util.parseArguments(invocation.arguments());
        if (arguments.length > 2) {
            return List.of();
        }
        if (pinSuggestion.test(invocation.arguments())) {
            return List.of("123456");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.REGISTER_COMMAND);
    }
}
