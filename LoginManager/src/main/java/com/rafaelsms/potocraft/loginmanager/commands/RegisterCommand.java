package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.LoginUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public class RegisterCommand extends Command implements TabExecutor {

    private final @NotNull LoginManagerPlugin plugin;

    public RegisterCommand(@NotNull LoginManagerPlugin plugin) {
        super("registrar", Permissions.COMMAND_REGISTER, "reg", "register", "cadastrar");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Prevent console from executing
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Offline players only
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        // Retrieve profile
        Profile profile;
        try {
            profile = plugin.getDatabase()
                            .getProfile(player.getUniqueId())
                            .orElse(new Profile(player.getUniqueId(), player.getName()));
        } catch (Exception ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Check if profile already has a password
        if (profile.hasPassword()) {
            if (LoginUtil.isPlayerLoggedIn(plugin, profile, player) &&
                sender.hasPermission(Permissions.COMMAND_CHANGE_PASSWORD)) {
                player.sendMessage(plugin.getConfiguration().getCommandRegisterShouldChangePasswordInstead());
            } else {
                player.sendMessage(plugin.getConfiguration().getCommandRegisterShouldLoginInstead());
            }
            return;
        }

        // Check if too many uses for this IP
        try {
            Optional<InetSocketAddress> address = LoginUtil.getInetAddress(player.getSocketAddress());
            if (address.isPresent()) {
                long count = plugin.getDatabase().getAddressUsageCount(address.get());
                if (count >= plugin.getConfiguration().getMaxAccountsPerAddress()) {
                    player.disconnect(plugin.getConfiguration().getCommandRegisterAccountLimitForAddress());
                    return;
                }
            }
        } catch (Exception ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Retrieve arguments
        if (args.length != 2 || !LoginUtil.isValidPassword(args[0]) || !LoginUtil.isValidPassword(args[1])) {
            player.sendMessage(plugin.getConfiguration().getCommandRegisterHelp());
            return;
        }

        // Check if confirmation is valid
        String passwordString = args[0];
        String passwordConfirmationString = args[1];
        if (!passwordString.equalsIgnoreCase(passwordConfirmationString)) {
            player.sendMessage(plugin.getConfiguration().getCommandRegisterPasswordsDoNotMatch());
            return;
        }

        // Check if we couldn't set password
        if (!profile.setPassword(passwordString)) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPasswordFormat());
            return;
        }

        // Save status
        try {
            profile.setLoggedIn(LoginUtil.getInetAddress(player.getSocketAddress()).orElse(null));
            plugin.getDatabase().saveProfile(profile);
            player.sendMessage(plugin.getConfiguration().getCommandLoggedIn());
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }

        // Send player to default server
        LoginUtil.sendPlayerToDefaultServer(plugin, player);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return List.of("senhaSegura", "098765");
        }
        return List.of();
    }
}
