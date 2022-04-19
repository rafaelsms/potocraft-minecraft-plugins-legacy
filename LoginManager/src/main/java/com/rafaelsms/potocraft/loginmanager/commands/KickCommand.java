package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class KickCommand extends Command implements TabExecutor {

    // /kick <name> <reason>

    private final @NotNull LoginManagerPlugin plugin;

    public KickCommand(@NotNull LoginManagerPlugin plugin) {
        super("kick", Permissions.COMMAND_KICK, "expulsar");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfiguration().getCommandKickHelp());
            return;
        }

        String username = args[0];
        Optional<String> reason = TextUtil.joinStrings(args, 1);

        Optional<ProxiedPlayer> playerOptional =
                TextUtil.closestMatch(plugin.getProxy().getPlayers(), ProxiedPlayer::getName, username);
        if (playerOptional.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getNoPlayerFound());
            return;
        }
        ProxiedPlayer player = playerOptional.get();
        if (player.hasPermission(Permissions.COMMAND_KICK_EXEMPT)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        Profile profile;
        try {
            profile = plugin.getDatabase().getProfile(player.getUniqueId()).orElse(null);
        } catch (DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getCommandFailedToSearchProfile());
            return;
        }
        if (profile == null) {
            player.disconnect(new ComponentBuilder().create());
            sender.sendMessage(plugin.getConfiguration().getPlayerPunished(player.getName()));
            return;
        }

        try {
            profile.addReportEntry(ReportEntry.Type.KICK, getId(sender), null, reason.orElse(null));
            plugin.getDatabase().saveProfile(profile);
            @NotNull BaseComponent[] kickedMessage =
                    plugin.getConfiguration().getPunishmentMessageKicked(getName(sender), reason.orElse(null));
            player.disconnect(kickedMessage);
            sender.sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getCommandFailedToSaveProfile());
        }
    }

    private @Nullable UUID getId(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return player.getUniqueId();
        }
        return null;
    }

    private @Nullable String getName(CommandSender source) {
        if (source instanceof ProxiedPlayer player) {
            return player.getName();
        }
        return null;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Util.convertList(plugin.getProxy().getPlayers(), ProxiedPlayer::getName);
    }
}
