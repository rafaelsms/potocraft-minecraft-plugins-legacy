package com.rafaelsms.potocraft.util;

import com.rafaelsms.potocraft.Logger;
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
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class WorldGuardUtil {

    private WorldGuardUtil() {
    }

    public static @NotNull BlockVector3 toBlockVector3(@NotNull Location location) {
        return BukkitAdapter.adapt(location).toVector().toBlockPoint();
    }

    public static @NotNull Optional<LocalPlayer> toLocalPlayer(@NotNull Player player) {
        return Optional.ofNullable(WorldGuardPlugin.inst()).map(inst -> inst.wrapPlayer(player));
    }

    public static ProtectedCuboidRegion getRegion(@NotNull String id,
                                                  boolean isTransient,
                                                  @NotNull Location lowestPoint,
                                                  @NotNull Location highestPoint) {
        return new ProtectedCuboidRegion(id,
                                         isTransient,
                                         WorldGuardUtil.toBlockVector3(lowestPoint),
                                         WorldGuardUtil.toBlockVector3(highestPoint));
    }

    public static @NotNull Optional<WorldGuard> getWorldGuard() {
        return Optional.ofNullable(WorldGuard.getInstance());
    }

    public static @NotNull Optional<RegionManager> getRegionManager(@NotNull Player player) {
        return getRegionManager(player.getWorld());
    }

    public static @NotNull Optional<RegionManager> getRegionManager(@NotNull World world) {
        return getWorldGuard().map(wg -> wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)));
    }

    public static @NotNull Optional<StateFlag> registerFlag(@NotNull Logger logger,
                                                            @NotNull String flagName,
                                                            boolean defaultValue,
                                                            @NotNull RegionGroup defaultRegionGroup) {
        Optional<WorldGuard> worldGuard = getWorldGuard();
        if (worldGuard.isEmpty()) {
            logger.warn("WorldGuard not present, skipping {} custom flag registry.", flagName);
            return Optional.empty();
        }
        FlagRegistry flagRegistry = worldGuard.get().getFlagRegistry();
        try {
            // Default flag value is false, so every player will have its item damaged by default
            StateFlag flag = new StateFlag(flagName, defaultValue, defaultRegionGroup);
            flagRegistry.register(flag);
            return Optional.of(flag);
        } catch (FlagConflictException exception) {
            logger.warn("Failed to register {} flag, not doing anything:", flagName, exception);
            return Optional.empty();
        }
    }

    public static @NotNull Optional<ApplicableRegionSet> getApplicableRegions(@NotNull Location location) {
        return getRegionManager(location.getWorld()).map(rm -> rm.getApplicableRegions(toBlockVector3(location)));
    }
}
