package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ListCommand implements RawCommand {

    private final @NotNull LoginManagerPlugin plugin;

    public ListCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            printServerList(invocation.source(), server.getServerInfo().getName(), server.getPlayersConnected());
        }
        ArrayList<Player> limboPlayers = new ArrayList<>();
        for (Player player : plugin.getServer().getAllPlayers()) {
            if (player.getCurrentServer().isEmpty()) {
                limboPlayers.add(player);
            }
        }
        if (!limboPlayers.isEmpty()) {
            printServerList(invocation.source(), "limbo?", limboPlayers);
        }
    }

    private void printServerList(@NotNull CommandSource source,
                                 @NotNull String serverName,
                                 @NotNull Collection<Player> playerList) {
        source.sendMessage(plugin.getConfiguration().getListServerList(serverName, playerList));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_LIST);
    }
}
