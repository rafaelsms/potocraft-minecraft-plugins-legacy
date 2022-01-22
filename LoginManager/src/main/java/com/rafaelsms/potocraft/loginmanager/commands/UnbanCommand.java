package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.command.RawCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnbanCommand implements RawCommand {

    private final Pattern commandSyntax = Pattern.compile("^\\s*(\\S+).*$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public UnbanCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandUnbanHelp());
            return;
        }

        List<Profile> offlineProfiles;
        try {
            String nameRegex = matcher.group(1);
            offlineProfiles = plugin.getDatabase().getOfflineProfiles(nameRegex);
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        Optional<Profile> profileOptional = Util.handleUniqueProfile(plugin, invocation.source(), offlineProfiles);
        if (profileOptional.isEmpty()) {
            return;
        }
        Profile profile = profileOptional.get();

        boolean changed = false;
        for (ReportEntry reportEntry : profile.getReportEntries()) {
            // Inactive the report entry that prevents joining
            if (reportEntry.isPreventingJoin()) {
                reportEntry.setActive(false);
                changed = true;
                break;
            }
        }

        if (changed) {
            invocation
                    .source()
                    .sendMessage(plugin.getConfiguration().getCommandUnpunished(profile.getLastPlayerName()));
        } else {
            invocation
                    .source()
                    .sendMessage(plugin.getConfiguration().getCommandPlayerIsNotPunished(profile.getLastPlayerName()));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_UNBAN);
    }
}
