package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * We will prevent logged off players from: - issuing certain commands; - speaking.
 */
public class LoggedOffPlayerListener implements Listener {

    private final @NotNull LoginManagerPlugin plugin;

    public LoggedOffPlayerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO kick if player don't log in after X seconds

    @EventHandler
    public void preventLoggedOffChat(ChatEvent event) {
        // Ignore cancelled
        if (event.isCancelled()) {
            return;
        }
        // Ignore non-chat
        if (event.isCommand()) {
            return;
        }

        Util.getPlayer(event.getSender()).ifPresent(sender -> {
            // Ignore if logged on
            if (isPlayerLoggedOn(sender)) {
                return;
            }
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void preventLoggedOffUsingCommands(ChatEvent event) {
        // Ignore cancelled
        if (event.isCancelled()) {
            return;
        }
        // Ignore non-commands
        if (!event.isCommand()) {
            return;
        }

        Util.getPlayer(event.getSender()).ifPresent(sender -> {
            // Ignore if logged on
            if (isPlayerLoggedOn(sender)) {
                return;
            }

            // Split commands
            String[] words = event.getMessage().split(" ");
            if (words.length == 0) {
                event.setCancelled(true);
                return;
            }

            // Supposed to be a command
            String command = words[0];
            if (command.length() < 1) {
                event.setCancelled(true);
                return;
            }
            command = command.substring(1);

            // Filter out any commands that aren't on the allowed list
            if (!plugin.getConfiguration().getAllowedCommandsLoggedOff().contains(command)) {
                sender.sendMessage(plugin.getConfiguration().getPunishmentMessageLoggedOff());
                event.setCancelled(true);
            }
        });
    }

    private boolean isPlayerLoggedOn(ProxiedPlayer player) {
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin()) {
            return true;
        }
        Optional<Profile> profile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
        return profile.isPresent() && Util.isPlayerLoggedIn(plugin, profile.get(), player);
    }
}
