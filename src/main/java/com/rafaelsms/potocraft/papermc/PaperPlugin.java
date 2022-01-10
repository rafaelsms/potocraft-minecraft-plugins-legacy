package com.rafaelsms.potocraft.papermc;

import com.rafaelsms.potocraft.common.CommonServer;
import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.Plugin;
import com.rafaelsms.potocraft.common.util.PluginType;
import com.rafaelsms.potocraft.papermc.listeners.ProfileUpdater;
import com.rafaelsms.potocraft.papermc.listeners.UserListener;
import com.rafaelsms.potocraft.papermc.profile.PaperProfile;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.papermc.user.PaperUserManager;
import com.rafaelsms.potocraft.papermc.util.PaperDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class PaperPlugin extends JavaPlugin implements Plugin<PaperProfile, PaperUser, Player> {

    private final @NotNull CommonServer commonServer = new PaperServer(this);

    private @Nullable PaperSettings settings = null;
    private @Nullable PaperDatabase database = null;
    private @Nullable PaperUserManager userManager = null;

    @Override
    public void onEnable() {
        try {
            this.settings = new PaperSettings(this);
            this.database = new PaperDatabase(this);
            this.userManager = new PaperUserManager(this);
        } catch (Exception exception) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register permissions on Bukkit
        for (String permission : Permissions.values) {
            Permission bukkitPermission = getServer().getPluginManager().getPermission(permission);
            if (bukkitPermission == null) {
                getServer().getPluginManager().addPermission(new Permission(permission));
            }
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new ProfileUpdater(this), this);
        getServer().getPluginManager().registerEvents(new UserListener(getUserManager()), this);

        logger().info("PotoCraft Paper Plugin enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public @NotNull CommonServer getCommonServer() {
        return commonServer;
    }

    @Override
    public @NotNull PluginType getPluginType() {
        return PluginType.PAPER;
    }

    @Override
    public @NotNull PaperSettings getSettings() {
        assert settings != null;
        return settings;
    }

    @Override
    public @NotNull Logger logger() {
        return getSLF4JLogger();
    }

    @Override
    public @NotNull PaperDatabase getDatabase() {
        assert database != null;
        return database;
    }

    @Override
    public @NotNull PaperUserManager getUserManager() {
        assert userManager != null;
        return userManager;
    }
}
