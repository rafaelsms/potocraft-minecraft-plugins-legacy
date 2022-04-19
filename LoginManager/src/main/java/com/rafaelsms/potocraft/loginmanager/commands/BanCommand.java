package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BanCommand extends Command implements TabExecutor {

    // /ban <name> <reason>

    private final @NotNull LoginManagerPlugin plugin;

    public BanCommand(@NotNull LoginManagerPlugin plugin) {
        super("ban", Permissions.COMMAND_BAN, "banir");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfiguration().getCommandBanHelp());
            return;
        }

        Optional<String> reason = TextUtil.joinStrings(args, 1);

        Optional<Profile> profileOptional = CommandUtil.handlePlayerSearch(plugin, sender, args[0]);
        if (profileOptional.isEmpty()) {
            return;
        }

        Profile profile = profileOptional.get();
        Optional<ProxiedPlayer> optionalPlayer = getOnlinePlayer(profile.getPlayerId());
        if (!sender.hasPermission(Permissions.COMMAND_BAN_OFFLINE) && optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getCommandBanPlayerOffline());
            return;
        }
        if (optionalPlayer.isPresent() && optionalPlayer.get().hasPermission(Permissions.COMMAND_BAN_EXEMPT)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        try {
            profile.addReportEntry(ReportEntry.Type.BAN, getId(sender), null, reason.orElse(null));
            plugin.getDatabase().saveProfile(profile);
            optionalPlayer.ifPresent(player -> {
                @NotNull BaseComponent[] messageBanned = plugin.getConfiguration()
                                                               .getPunishmentMessageBanned(getName(sender),
                                                                                           null,
                                                                                           reason.orElse(null));
                player.disconnect(messageBanned);
            });
            sender.sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getCommandFailedToSaveProfile());
        }
    }

    private Optional<ProxiedPlayer> getOnlinePlayer(@NotNull UUID playerId) {
        return Optional.ofNullable(plugin.getProxy().getPlayer(playerId));
    }

    private @Nullable UUID getId(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return player.getUniqueId();
        }
        return null;
    }

    private @Nullable String getName(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return player.getName();
        }
        return null;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Util.convertList(plugin.getProxy().getPlayers(), ProxiedPlayer::getName);
    }
}
