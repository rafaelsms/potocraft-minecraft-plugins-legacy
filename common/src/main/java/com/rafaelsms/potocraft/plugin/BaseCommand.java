package com.rafaelsms.potocraft.plugin;

import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.plugin.player.BaseProfile;
import com.rafaelsms.potocraft.plugin.player.BaseUser;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class BaseCommand<User extends BaseUser<? extends BaseProfile>> implements CommandExecutor {

    private final @NotNull BaseJavaPlugin<User, ? extends BaseProfile, ? extends BaseDatabase, ? extends BaseConfiguration, ? extends BasePermissions>
            plugin;
    private final @NotNull Permission permission;

    public BaseCommand(@NotNull BaseJavaPlugin<User, ? extends BaseProfile, ? extends BaseDatabase, ? extends BaseConfiguration, ? extends BasePermissions> plugin,
                       @NotNull Permission permission) {
        this.plugin = plugin;
        this.permission = permission;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        if (sender instanceof Player player) {
            User user = plugin.getUserManager().getUser(player);
            onPlayerCommand(user, label, args);
        } else {
            onConsoleCommand(sender, label, args);
        }
        return true;
    }

    public void onConsoleCommand(@NotNull CommandSender sender, String label, String[] arguments) {
        sender.sendMessage(plugin.getConfiguration().getCommandIsPlayerOnly());
    }

    public abstract void onPlayerCommand(@NotNull User user, String label, String[] arguments);

    protected @NotNull Optional<User> searchOnlinePlayer(@NotNull String playerName) {
        return TextUtil.closestMatch(plugin.getServer().getOnlinePlayers(), Player::getName, playerName)
                       .map(player -> plugin.getUserManager().getUser(player));
    }
}
