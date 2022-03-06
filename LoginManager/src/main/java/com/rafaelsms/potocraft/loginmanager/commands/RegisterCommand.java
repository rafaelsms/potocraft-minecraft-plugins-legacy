package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.rafaelsms.potocraft.util.TextUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class RegisterCommand extends Command implements TabExecutor {

    private final Pattern pinFormat = Pattern.compile("^(\\d{6})$", Pattern.CASE_INSENSITIVE);

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

        // Check if profile already has a PIN
        if (profile.hasPin()) {
            if (Util.isPlayerLoggedIn(plugin, profile, player) &&
                sender.hasPermission(Permissions.COMMAND_CHANGE_PIN)) {
                player.sendMessage(plugin.getConfiguration().getCommandRegisterShouldChangePinInstead());
            } else {
                player.sendMessage(plugin.getConfiguration().getCommandRegisterShouldLoginInstead());
            }
            return;
        }

        // Check if too many uses for this IP
        try {
            Optional<InetSocketAddress> address = Util.getInetAddress(player.getSocketAddress());
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
        if (args.length != 2 || !pinFormat.matcher(args[0]).matches() || !pinFormat.matcher(args[1]).matches()) {
            player.sendMessage(plugin.getConfiguration().getCommandRegisterHelp());
            return;
        }

        // Parse to PIN
        String pinString = args[0];
        String pinConfirmationString = args[1];
        Optional<Integer> pinOptional = TextUtil.parseMatchingPins(pinString, pinConfirmationString);
        if (pinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandRegisterInvalidPin());
            return;
        }

        // Check if we couldn't set pin
        if (!profile.setPin(pinOptional.get())) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPinFormat());
            return;
        }

        // Save status
        try {
            profile.setLoggedIn(Util.getInetAddress(player.getSocketAddress()).orElse(null));
            player.sendMessage(plugin.getConfiguration().getCommandLoggedIn());
            plugin.getDatabase().saveProfile(profile);
            sender.sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }

        // Send player to default server
        Util.sendPlayerToDefaultServer(plugin, player);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return List.of("123456", "098765");
        }
        return List.of();
    }
}
