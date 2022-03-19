package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VipCommand implements CommandExecutor {

    private final @NotNull ServerUtilityPlugin plugin;

    public VipCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.COMMAND_VIP)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
            for (Node node : user.resolveDistinctInheritedNodes(QueryOptions.builder(QueryMode.CONTEXTUAL)
                                                                            .flag(Flag.RESOLVE_INHERITANCE, true)
                                                                            .build())) {
                if (!(node instanceof InheritanceNode inheritanceNode)) {
                    continue;
                }
                if (inheritanceNode.getGroupName().equalsIgnoreCase(plugin.getConfiguration().getVipGroupName())) {
                    player.sendMessage(plugin.getConfiguration().getVipRemainingTime(inheritanceNode));
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        player.sendMessage(plugin.getConfiguration().getVipRemainingTime(null));
        return true;
    }
}
