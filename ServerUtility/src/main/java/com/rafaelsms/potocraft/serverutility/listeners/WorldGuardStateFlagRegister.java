package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class WorldGuardStateFlagRegister implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;
    private final @NotNull String flagName;
    private final boolean defaultValue;
    private final @NotNull RegionGroup regionGroup;

    private StateFlag flag = null;

    public WorldGuardStateFlagRegister(@NotNull ServerUtilityPlugin plugin,
                                       @NotNull String flagName,
                                       boolean defaultValue,
                                       @NotNull RegionGroup regionGroup) {
        this.plugin = plugin;
        this.flagName = flagName;
        this.defaultValue = defaultValue;
        this.regionGroup = regionGroup;
        registerFlag();
    }

    private void registerFlag() {
        Optional<WorldGuard> worldGuard = plugin.getWorldGuard();
        if (worldGuard.isEmpty()) {
            plugin.logger().warn("WorldGuard not present, skipping {} custom flag registry.", flagName);
            return;
        }
        FlagRegistry flagRegistry = worldGuard.get().getFlagRegistry();
        try {
            // Default flag value is false, so every player will have its item damaged by default
            StateFlag flag = new StateFlag(flagName, defaultValue, regionGroup);
            flagRegistry.register(flag);
            this.flag = flag;
        } catch (FlagConflictException exception) {
            plugin.logger().warn("Failed to register {} flag, not doing anything:", flagName, exception);
        }
    }

    protected @NotNull Optional<Boolean> testFlag(@NotNull Location location, @Nullable Player player) {
        Optional<WorldGuard> worldGuard = plugin.getWorldGuard();
        if (worldGuard.isEmpty() || flag == null) {
            return Optional.empty();
        }
        RegionManager regionManager =
                worldGuard.get().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) {
            plugin.logger().error("No region manager found! Preventing item damage as a precaution.");
            return Optional.empty();
        }

        BlockVector3 blockVector3 = BukkitAdapter.adapt(location).toVector().toBlockPoint();
        ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(blockVector3);
        LocalPlayer localPlayerOptional =
                Optional.ofNullable(player).map(p -> WorldGuardPlugin.inst().wrapPlayer(p)).orElse(null);
        return Optional.of(applicableRegions.testState(localPlayerOptional, flag));
    }
}
