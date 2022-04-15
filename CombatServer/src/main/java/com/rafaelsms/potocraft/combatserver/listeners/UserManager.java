package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import com.rafaelsms.potocraft.combatserver.player.Profile;
import com.rafaelsms.potocraft.combatserver.player.User;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.plugin.player.BaseUserManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends BaseUserManager<User, Profile> {

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
    protected @NotNull Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws DatabaseException {
        return Profile.fetch(plugin, event.getUniqueId());
    }

    @Override
    protected @NotNull User retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) {
        return new User(plugin, event.getPlayer(), profile);
    }

    @Override
    protected void onLogin(@NotNull User user) {
    }

    @Override
    protected void onJoin(@NotNull User user) {
    }

    @Override
    protected void onQuit(@NotNull User user) {
    }

    @Override
    protected void tickUser(@NotNull User user) {
    }

    @Override
    protected void saveUser(@NotNull User user) throws DatabaseException {
        user.getProfile().save();
    }
}
