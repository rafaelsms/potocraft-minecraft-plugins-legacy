package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.rafaelsms.potocraft.util.TextUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class LoginCommand extends Command implements TabExecutor {

    private final Pattern pinExtractor = Pattern.compile("^\\d{6}$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public LoginCommand(@NotNull LoginManagerPlugin plugin) {
        super("login", Permissions.COMMAND_LOGIN, "l", "log");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Check player connection type, as online players should not be able to use this command
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        if (args.length != 1 || !pinExtractor.matcher(args[0]).matches()) {
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
        Optional<Integer> pinOptional = TextUtil.parsePin(args[0]);
        if (pinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandLoginHelp());
            return;
        }

        // Check if we couldn't set pin
        if (!profile.isPinValid(pinOptional.get())) {
            player.disconnect(plugin.getConfiguration().getCommandIncorrectPin());
            return;
        }

        if (pinOptional.get().equals(98765) || pinOptional.get().equals(123456)) {
            for (int i = 0; i < 3; i++) {
                player.sendMessage(plugin.getConfiguration().getCommandLoginWithWeakPin());
            }
        }

        try {
            // Set as logged in and save
            profile.setLoggedIn(Util.getInetAddress(player.getSocketAddress()).orElse(null));
            player.sendMessage(plugin.getConfiguration().getCommandLoggedIn());
            plugin.getDatabase().saveProfile(profile);
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }

        // Attempt to send player to lobby
        if (profile.getLastServerName().isPresent()) {
            ServerInfo serverInfo = plugin.getProxy().getServerInfo(profile.getLastServerName().get());
            if (serverInfo != null) {
                player.connect(serverInfo, (result, error) -> {
                    if (error != null || result == null || !result) {
                        Util.sendPlayerToDefaultServer(plugin, player);
                    }
                }, ServerConnectEvent.Reason.PLUGIN);
                return;
            }
        }
        Util.sendPlayerToDefaultServer(plugin, player);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return List.of("123456", "098765");
        }
        return List.of();
    }
}
