package com.rafaelsms.teleporter;

import com.rafaelsms.potocraft.player.BaseUser;
import com.rafaelsms.potocraft.player.BaseUserManager;
import com.rafaelsms.potocraft.plugin.BaseJavaPlugin;
import com.rafaelsms.teleporter.player.Profile;
import com.rafaelsms.teleporter.player.UserManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TeleporterPlugin extends BaseJavaPlugin<BaseUser, Profile, Database, Configuration> {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;

    public TeleporterPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
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
    public @NotNull BaseUserManager<BaseUser, Profile> getUserManager() {
        return userManager;
    }

    @Override
    public @NotNull Database getDatabase() {
        return database;
    }
}
