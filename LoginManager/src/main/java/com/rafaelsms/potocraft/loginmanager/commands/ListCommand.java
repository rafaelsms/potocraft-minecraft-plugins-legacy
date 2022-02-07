package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ListCommand extends Command {

    private final @NotNull LoginManagerPlugin plugin;

    public ListCommand(@NotNull LoginManagerPlugin plugin) {
        super("list", Permissions.COMMAND_LIST, "lista", "jogadores", "players", "online");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServersCopy().entrySet()) {
            printServerList(sender, entry.getKey(), entry.getValue().getPlayers());
        }
        ArrayList<ProxiedPlayer> limboPlayers = new ArrayList<>();
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            if (player.getServer() == null) {
                limboPlayers.add(player);
            }
        }
        if (!limboPlayers.isEmpty()) {
            printServerList(sender, "limbo?", limboPlayers);
        }
    }

    private void printServerList(@NotNull CommandSender source,
                                 @NotNull String serverName,
                                 @NotNull Collection<ProxiedPlayer> playerList) {
        source.sendMessage(plugin.getConfiguration().getListServerList(serverName, playerList));
    }
}
