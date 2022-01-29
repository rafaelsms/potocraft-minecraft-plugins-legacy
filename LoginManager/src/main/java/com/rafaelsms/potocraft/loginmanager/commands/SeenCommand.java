package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import com.velocitypowered.api.command.RawCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeenCommand implements RawCommand {

    // /seen <name>

    private final Pattern commandSyntax = Pattern.compile("^\\s*(\\S+)\\s*$");

    private final @NotNull LoginManagerPlugin plugin;

    public SeenCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandSeenHelp());
            return;
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException ignored) {
        }

        Profile profile;
        if (uuid != null) {
            try {
                Optional<Profile> profileOptional = plugin.getDatabase().getProfile(uuid);
                if (profileOptional.isEmpty()) {
                    invocation.source().sendMessage(plugin.getConfiguration().getCommandNoProfileFound());
                    return;
                }
                profile = profileOptional.get();
            } catch (Database.DatabaseException ignored) {
                invocation.source().sendMessage(plugin.getConfiguration().getCommandFailedToSearchProfile());
                return;
            }
        } else {
            Optional<Profile> profileOptional =
                    CommandUtil.handlePlayerSearch(plugin, invocation.source(), matcher.group(1));
            if (profileOptional.isEmpty()) {
                return;
            }
            profile = profileOptional.get();
        }
        invocation.source().sendMessage(plugin.getConfiguration().getCommandSeen(plugin, profile));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_SEEN);
    }
}
