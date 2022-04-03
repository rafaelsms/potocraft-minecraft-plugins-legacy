package com.rafaelsms.teleporter.player;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.player.BaseUser;
import com.rafaelsms.potocraft.player.BaseUserManager;
import com.rafaelsms.teleporter.TeleporterPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends BaseUserManager<BaseUser, Profile> {

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
    protected Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws DatabaseException {
        return null;
    }

    @Override
    protected BaseUser retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) {
        return null;
    }

    @Override
    protected void onLogin(BaseUser user) {

    }

    @Override
    protected void onJoin(BaseUser user) {

    }

    @Override
    protected void onQuit(BaseUser user) {

    }

    @Override
    protected void tickUser(BaseUser user) {

    }

    @Override
    protected void saveUser(BaseUser user) throws DatabaseException {

    }
}
