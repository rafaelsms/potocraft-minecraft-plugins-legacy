package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.command.RawCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryCommand implements RawCommand {

    // /history <name>

    private final Pattern commandSyntax = Pattern.compile("^\\s*(\\S+)\\s*$");

    private final @NotNull LoginManagerPlugin plugin;

    public HistoryCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandHistoryHelp());
            return;
        }

        String usernameRegex = matcher.group(1);

        List<Profile> offlineProfiles;
        try {
            offlineProfiles = plugin.getDatabase().getOfflineProfiles(usernameRegex);
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        Optional<Profile> profileOptional = Util.handleUniqueProfile(plugin, invocation.source(), offlineProfiles);
        if (profileOptional.isEmpty()) {
            return;
        }
        Profile profile = profileOptional.get();

        // TODO
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_HISTORY);
    }
}
