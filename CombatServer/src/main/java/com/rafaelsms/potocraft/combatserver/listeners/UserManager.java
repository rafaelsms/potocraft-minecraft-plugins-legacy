package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import com.rafaelsms.potocraft.combatserver.player.Profile;
import com.rafaelsms.potocraft.combatserver.player.User;
import com.rafaelsms.potocraft.database.DatabaseException;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends com.rafaelsms.potocraft.util.UserManager<User, Profile> {

    private final @NotNull CombatServerPlugin plugin;

    public UserManager(@NotNull CombatServerPlugin plugin) {
        super(plugin, plugin.getConfiguration().getSaveProfileTaskTimer());
        this.plugin = plugin;
    }

    @Override
    protected Component getKickMessageCouldNotLoadProfile() {
        return plugin.getConfiguration().getFailedToRetrieveProfile();
    }

    @Override
    protected Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws DatabaseException {
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
        // Update player name
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
    }

    @Override
    protected void saveUser(User user) {
        plugin.getDatabase().saveProfile(user.getProfile());
    }
}
