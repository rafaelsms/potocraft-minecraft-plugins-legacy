package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import com.rafaelsms.potocraft.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UnmuteCommand extends Command implements TabExecutor {

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
            } catch (DatabaseException ignored) {
                sender.sendMessage(plugin.getConfiguration().getCommandFailedToSaveProfile());
                return;
            }
            sender.sendMessage(plugin.getConfiguration().getCommandUnpunished(profile.getLastPlayerName()));
        } else {
            sender.sendMessage(plugin.getConfiguration().getCommandPlayerIsNotPunished(profile.getLastPlayerName()));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Util.convertList(plugin.getProxy().getPlayers(), ProxiedPlayer::getName);
    }
}
