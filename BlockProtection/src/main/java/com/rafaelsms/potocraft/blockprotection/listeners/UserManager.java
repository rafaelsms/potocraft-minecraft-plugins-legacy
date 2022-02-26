package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.Profile;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.database.Database;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends com.rafaelsms.potocraft.util.UserManager<User, Profile> {

    private final @NotNull BlockProtectionPlugin plugin;

    public UserManager(@NotNull BlockProtectionPlugin plugin) {
        super(plugin, plugin.getConfiguration().getMongoSavePlayersTaskTimer());
        this.plugin = plugin;
    }

    @Override
    protected Component getKickMessageCouldNotLoadProfile() {
        return plugin.getConfiguration().getFailedToFetchProfile();
    }

    @Override
    protected Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws Database.DatabaseException {
        return plugin.getDatabase()
                     .getProfile(event.getUniqueId())
                     .orElse(new Profile(event.getUniqueId(), event.getName()));
    }

    @Override
    protected User retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) {
        return new User(plugin, event.getPlayer(), profile);
    }

    @Override
    protected void onLogin(User user) {
        user.getProfile().setPlayerName(user.getPlayer().getName());
    }

    @Override
    protected void onJoin(User user) {
    }

    @Override
    protected void onQuit(User user) {
    }

    @Override
    protected void tickUser(User user) {
        user.run();
    }

    @Override
    protected void saveUser(User user) {
        plugin.getDatabase().saveProfileCatching(user.getProfile());
    }
}
