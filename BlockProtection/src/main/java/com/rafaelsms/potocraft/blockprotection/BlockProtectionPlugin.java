package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.blockprotection.commands.ProtectCommand;
import com.rafaelsms.potocraft.blockprotection.listeners.AreaListener;
import com.rafaelsms.potocraft.blockprotection.listeners.UserManager;
import com.rafaelsms.potocraft.blockprotection.util.ProtectionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
        getServer().getPluginManager().registerEvents(new AreaListener(this), this);

        registerCommand("protect", new ProtectCommand(this));

        logger().info("BlockProtection enabled!");
    }

    @Override
    public void onDisable() {
        // Save all WorldGuard
        getWorldGuardInstance().ifPresent(worldGuard -> {
            for (RegionManager regionManager : worldGuard.getPlatform().getRegionContainer().getLoaded()) {
                try {
                    regionManager.save();
                    logger().info("Saved data for WorldGuard's {} region manager", regionManager.getName());
                } catch (StorageException exception) {
                    logger().error("Failed to save WorldGuard data:", exception);
                }
            }
        });

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
        return getRegionManager(player.getWorld());
    }

    public @NotNull Optional<RegionManager> getRegionManager(@NotNull World world) {
        try {
            return Optional.of(getRegionManagerInstance(world));
        } catch (ProtectionException ignored) {
            return Optional.empty();
        }
    }

    public @NotNull RegionManager getRegionManagerInstance(@NotNull World world) throws ProtectionException {
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

    public @NotNull Optional<WorldGuard> getWorldGuardInstance() {
        try {
            return Optional.of(getWorldGuard());
        } catch (ProtectionException ignored) {
            return Optional.empty();
        }
    }

    public @NotNull WorldGuard getWorldGuard() throws ProtectionException {
        WorldGuard instance = WorldGuard.getInstance();
        if (instance == null) {
            throw new ProtectionException("WorldGuard instance is not available.");
        }
        return instance;
    }

    private void registerCommand(@NotNull String commandName, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(commandName);
        assert pluginCommand != null;
        pluginCommand.setExecutor(executor);
    }

    public Optional<ProtectedRegion> getBaseRegion(@NotNull World world) {
        Optional<RegionManager> managerOptional = getRegionManager(world);
        if (managerOptional.isEmpty()) {
            return Optional.empty();
        }
        RegionManager regionManager = managerOptional.get();
        ProtectedRegion baseRegion = regionManager.getRegion(getConfiguration().getBaseGlobalRegionId());
        if (baseRegion == null) {
            baseRegion = new GlobalProtectedRegion(getConfiguration().getBaseGlobalRegionId());
            // Set default flags for regions
            baseRegion.setFlag(Flags.USE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.USE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.DAMAGE_ANIMALS, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.DAMAGE_ANIMALS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.DAMAGE_ANIMALS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.TRAMPLE_BLOCKS, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.TRAMPLE_BLOCKS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.CHEST_ACCESS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.TNT, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.TNT.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.ENDERPEARL.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.CHORUS_TELEPORT, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.CHORUS_TELEPORT.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.POTION_SPLASH, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.POTION_SPLASH.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.GREET_TITLE, getConfiguration().getGreetingTitle());
            baseRegion.setFlag(Flags.FAREWELL_TITLE, getConfiguration().getLeavingTitle());
            // Anti-griefing measures
            // Creeper, wither are not denied, you should prevent block damage through WorldGuard's configuration file
            baseRegion.setFlag(Flags.ENDERDRAGON_BLOCK_DAMAGE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.RAVAGER_RAVAGE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.FIREWORK_DAMAGE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.FIREWORK_DAMAGE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.OTHER_EXPLOSION.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            baseRegion.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
            try {
                // Save region
                regionManager.addRegion(baseRegion);
                regionManager.saveChanges();
            } catch (StorageException exception) {
                logger().error("Failed to save WorldGuard base region for {}:", world.getName(), exception);
                return Optional.empty();
            }
        }
        return Optional.of(baseRegion);
    }
}
