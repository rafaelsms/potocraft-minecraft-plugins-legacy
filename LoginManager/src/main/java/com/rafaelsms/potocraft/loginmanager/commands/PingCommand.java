package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PingCommand extends Command implements TabExecutor {

    // /kick <name> <reason>

    private final @NotNull LoginManagerPlugin plugin;

    public PingCommand(@NotNull LoginManagerPlugin plugin) {
        super("ping", Permissions.COMMAND_PING, "delay");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (!(sender instanceof ProxiedPlayer player)) {
                sender.sendMessage(plugin.getConfiguration().getCommandPingHelp());
                return;
            }

            sender.sendMessage(plugin.getConfiguration().getCommandPing(player.getPing()));
            return;
        }
        if (!sender.hasPermission(Permissions.COMMAND_PING_OTHERS)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        String username = args[0];
        Optional<ProxiedPlayer> playerOptional =
                TextUtil.closestMatch(plugin.getProxy().getPlayers(), ProxiedPlayer::getName, username);
        if (playerOptional.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getNoPlayerFound());
            return;
        }
        sender.sendMessage(plugin.getConfiguration()
                                 .getCommandPingOthers(playerOptional.get().getName(), playerOptional.get().getPing()));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return Util.convertList(plugin.getProxy().getPlayers(), ProxiedPlayer::getName);
    }
}
