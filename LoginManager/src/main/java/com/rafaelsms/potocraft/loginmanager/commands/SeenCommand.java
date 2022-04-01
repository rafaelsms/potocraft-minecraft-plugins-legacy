package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import com.rafaelsms.potocraft.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class SeenCommand extends Command implements TabExecutor {

    // /seen <name>

    private final @NotNull LoginManagerPlugin plugin;

    public SeenCommand(@NotNull LoginManagerPlugin plugin) {
        super("seen", Permissions.COMMAND_SEEN, "info", "search", "buscar", "history", "historico");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getCommandSeenHelp());
            return;
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ignored) {
        }

        Profile profile;
        if (uuid != null) {
            try {
                Optional<Profile> profileOptional = plugin.getDatabase().getProfile(uuid);
                if (profileOptional.isEmpty()) {
                    sender.sendMessage(plugin.getConfiguration().getCommandNoProfileFound());
                    return;
                }
                profile = profileOptional.get();
            } catch (DatabaseException ignored) {
                sender.sendMessage(plugin.getConfiguration().getCommandFailedToSearchProfile());
                return;
            }
        } else {
            Optional<Profile> profileOptional = CommandUtil.handlePlayerSearch(plugin, sender, args[0]);
            if (profileOptional.isEmpty()) {
                return;
            }
            profile = profileOptional.get();
        }
        sender.sendMessage(plugin.getConfiguration().getCommandSeen(plugin, profile));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Util.convertList(plugin.getProxy().getPlayers(), ProxiedPlayer::getName);
    }
}
