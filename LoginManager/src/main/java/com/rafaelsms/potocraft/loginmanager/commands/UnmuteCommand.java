package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UnmuteCommand extends Command {

    private final @NotNull LoginManagerPlugin plugin;

    public UnmuteCommand(@NotNull LoginManagerPlugin plugin) {
        super("unmute", Permissions.COMMAND_UNMUTE, "desilenciar");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getCommandUnmuteHelp());
            return;
        }

        Optional<Profile> profileOptional = CommandUtil.handlePlayerSearch(plugin, sender, args[0]);
        if (profileOptional.isEmpty()) {
            return;
        }

        Profile profile = profileOptional.get();
        boolean changed = false;
        for (ReportEntry reportEntry : profile.getReportEntries()) {
            // Inactive the report entry that prevents joining
            if (reportEntry.isPreventingChat()) {
                reportEntry.setActive(false);
                changed = true;
                break;
            }
        }

        if (changed) {
            try {
                plugin.getDatabase().saveProfile(profile);
            } catch (Database.DatabaseException ignored) {
                sender.sendMessage(plugin.getConfiguration().getCommandFailedToSaveProfile());
                return;
            }
            sender.sendMessage(plugin.getConfiguration().getCommandUnpunished(profile.getLastPlayerName()));
        } else {
            sender.sendMessage(plugin.getConfiguration().getCommandPlayerIsNotPunished(profile.getLastPlayerName()));
        }
    }
}
