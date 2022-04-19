package com.rafaelsms.potocraft.blockprotection;

import com.rafaelsms.potocraft.BasePlugin;
import com.rafaelsms.potocraft.blockprotection.commands.ProtectCommand;
import com.rafaelsms.potocraft.blockprotection.listeners.AreaListener;
import com.rafaelsms.potocraft.blockprotection.listeners.UserManager;
import com.rafaelsms.potocraft.util.WorldGuardUtil;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class BlockProtectionPlugin extends BasePlugin {

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

        info("BlockProtection enabled!");
    }

    @Override
    public void onDisable() {
        // Save all WorldGuard
        WorldGuardUtil.getWorldGuard().ifPresent(worldGuard -> {
            for (RegionManager regionManager : worldGuard.getPlatform().getRegionContainer().getLoaded()) {
                try {
                    regionManager.save();
                    info("Saved data for WorldGuard's {} region manager", regionManager.getName());
                } catch (StorageException exception) {
                    error("Failed to save WorldGuard data:", exception);
                }
            }
        });

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        info("BlockProtection disabled!");
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

    public Optional<ProtectedRegion> getBaseRegion(@NotNull World world) {
        Optional<RegionManager> managerOptional = WorldGuardUtil.getRegionManager(world);
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
            baseRegion.setFlag(Flags.WITHER_DAMAGE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.ENDERDRAGON_BLOCK_DAMAGE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.RAVAGER_RAVAGE, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
            baseRegion.setFlag(Flags.FIREWORK_DAMAGE, StateFlag.State.DENY);
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
