package com.rafaelsms.teleporter;

import com.rafaelsms.potocraft.plugin.BaseJavaPlugin;
import com.rafaelsms.potocraft.plugin.player.BaseUserManager;
import com.rafaelsms.teleporter.player.Profile;
import com.rafaelsms.teleporter.player.User;
import com.rafaelsms.teleporter.player.UserManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TeleporterPlugin extends BaseJavaPlugin<User, Profile, Database, Configuration, Permissions> {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;
    private final @NotNull Permissions permissions;

    public TeleporterPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
        this.permissions = new Permissions(this);
    }

    @Override
    protected @NotNull String getPluginName() {
        return "Teleporter";
    }

    @Override
    protected void executeOnEnable() {
    }

    @Override
    protected void executeOnDisable() {
    }

    @Override
    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public @NotNull BaseUserManager<User, Profile> getUserManager() {
        return userManager;
    }

    @Override
    public @NotNull Database getDatabase() {
        return database;
    }

    @Override
    public @NotNull Permissions getPermissions() {
        return permissions;
    }
}
