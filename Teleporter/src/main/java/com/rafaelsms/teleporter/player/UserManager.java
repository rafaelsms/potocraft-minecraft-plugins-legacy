package com.rafaelsms.teleporter.player;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.plugin.player.BaseUserManager;
import com.rafaelsms.teleporter.TeleporterPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends BaseUserManager<User, Profile> {

    private final @NotNull TeleporterPlugin plugin;

    public UserManager(@NotNull TeleporterPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected Component getKickMessageCouldNotLoadProfile() {
        return null;
    }

    @Override
    protected @NotNull Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws DatabaseException {
        return new Profile(event.getUniqueId(), plugin);
    }

    @Override
    protected @NotNull User retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) throws DatabaseException {
        return new User(profile, event.getPlayer(), plugin);
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
}
