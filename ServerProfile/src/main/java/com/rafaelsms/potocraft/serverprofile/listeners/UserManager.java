package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.players.User;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends com.rafaelsms.potocraft.util.UserManager<User, Profile> {

    private final @NotNull ServerProfilePlugin plugin;

    public UserManager(@NotNull ServerProfilePlugin plugin) {
        super(plugin, plugin.getConfiguration().getSavePlayersTaskTimerTicks());
        this.plugin = plugin;
    }

    @Override
    protected Component getKickMessageCouldNotLoadProfile() {
        return plugin.getConfiguration().getKickMessageCouldNotLoadProfile();
    }

    @Override
    protected Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws Database.DatabaseException {
        return plugin.getDatabase()
                     .loadProfile(event.getUniqueId())
                     .orElse(new Profile(event.getUniqueId(), event.getName()));
    }

    @Override
    protected User retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) {
        return new User(plugin, event.getPlayer(), profile);
    }

    @Override
    protected void onLogin(User user) {
        // Update player name
        user.getProfile().setPlayerName(user.getPlayer().getName());
    }

    @Override
    protected void onJoin(User user) {
    }

    @Override
    protected void onQuit(User user) {
        // Update play time
        user.getProfile().setQuitTime();
        // The profile is already saved on common.UserManager
    }

    @Override
    protected void tickUser(User user) {
        user.runTick();
    }

    @Override
    protected void saveUser(User user) {
        plugin.getDatabase().saveProfileCatching(user.getProfile());
    }
}
