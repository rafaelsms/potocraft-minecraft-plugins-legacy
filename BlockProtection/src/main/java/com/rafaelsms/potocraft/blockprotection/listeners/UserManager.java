package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.Profile;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.plugin.player.BaseUserManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends BaseUserManager<User, Profile> {

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
        user.run();
    }

    @Override
    protected void saveUser(@NotNull User user) throws DatabaseException {
        user.getProfile().save();
    }
}
