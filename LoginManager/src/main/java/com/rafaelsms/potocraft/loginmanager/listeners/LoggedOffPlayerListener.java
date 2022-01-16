package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We will prevent logged off players from:
 * - issuing certain commands;
 * - speaking.
 */
public class LoggedOffPlayerListener {

    private final Pattern commandPattern = Pattern.compile("^\\s*(\\S+)(\\s+.*)?$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public LoggedOffPlayerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void preventLoggedOffChat(PlayerChatEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            if (isPlayerLoggedOn(event.getPlayer())) {
                continuation.resume();
                return;
            }
            // Just cancel chatting
            event.setResult(PlayerChatEvent.ChatResult.denied());
            continuation.resume();
        });
    }

    @Subscribe
    private void preventLoggedOffUsingCommands(CommandExecuteEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            if (!event.getResult().isAllowed()) {
                continuation.resume();
                return;
            }
            if (!(event.getCommandSource() instanceof Player player) || isPlayerLoggedOn(player)) {
                continuation.resume();
                return;
            }

            // Attempt to fit into the regex
            Matcher matcher = commandPattern.matcher(event.getCommand());
            if (!matcher.matches()) {
                plugin
                        .getLogger()
                        .warn("Didn't expected to not find a command match: \"%s\"".formatted(event.getCommand()));
                player.sendMessage(plugin.getConfiguration().getPunishmentMessageLoggedOff());
                event.setResult(CommandExecuteEvent.CommandResult.denied());
                continuation.resume();
                return;
            }

            // Filter out any commands that aren't on the allowed list
            String command = matcher.group(1).toLowerCase();
            if (!plugin.getConfiguration().getAllowedCommandsLoggedOff().contains(command)) {
                player.sendMessage(plugin.getConfiguration().getPunishmentMessageLoggedOff());
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }
            continuation.resume();
        });
    }

    private boolean isPlayerLoggedOn(@NotNull Player player) {
        if (player.isOnlineMode() || !PlayerType.get(player).requiresLogin()) {
            return true;
        }
        Optional<Profile> profile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
        return profile.isPresent() && Util.isPlayerLoggedIn(plugin, profile.get(), player);
    }
}
