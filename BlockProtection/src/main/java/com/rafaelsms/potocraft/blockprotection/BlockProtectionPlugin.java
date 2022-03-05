package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.blockprotection.commands.ProtectCommand;
import com.rafaelsms.potocraft.blockprotection.listeners.UserManager;
import com.rafaelsms.potocraft.blockprotection.listeners.VolumeListener;
import com.rafaelsms.potocraft.blockprotection.util.ProtectionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class BlockProtectionPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;

    public BlockProtectionPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(userManager.getListener(), this);
        getServer().getPluginManager().registerEvents(new VolumeListener(this), this);

        registerCommand("protect", new ProtectCommand(this));

        logger().info("BlockProtection enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("BlockProtection disabled!");
    }

    public Logger logger() {
        return getSLF4JLogger();
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull Database getDatabase() {
        return database;
    }

    public @NotNull UserManager getUserManager() {
        return userManager;
    }

    public @NotNull Optional<RegionManager> getRegionManager(@NotNull Player player) {
        try {
            return Optional.of(getRegionManager(player.getWorld()));
        } catch (ProtectionException ignored) {
            return Optional.empty();
        }
    }

    public @NotNull RegionManager getRegionManager(@NotNull World world) throws ProtectionException {
        WorldGuard instance = WorldGuard.getInstance();
        if (instance == null) {
            throw new ProtectionException("WorldGuard instance is not available.");
        }
        RegionManager regionManager = instance.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            throw new ProtectionException("WorldGuard database is not available.");
        }
        return regionManager;
    }

    private void registerCommand(@NotNull String commandName, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(commandName);
        assert pluginCommand != null;
        pluginCommand.setExecutor(executor);
    }
}
