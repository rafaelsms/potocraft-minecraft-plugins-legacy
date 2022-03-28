package com.rafaelsms.potocraft.blockprotection.util;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class WorldGuardUtil {

    public static final String GLOBAL_REGION = "__global__";

    // Private constructor
    private WorldGuardUtil() {
    }

    public static @NotNull BlockVector3 toBlockVector3(@NotNull Location location) {
        return BukkitAdapter.adapt(location).toVector().toBlockPoint();
    }

    public static @NotNull LocalPlayer toLocalPlayer(@NotNull Player player) {
        return WorldGuardPlugin.inst().wrapPlayer(player);
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

    public static @NotNull Optional<ProtectedRegion> getProtectedRegion(@NotNull BlockProtectionPlugin plugin,
                                                                        @NotNull Player player,
                                                                        boolean warnPlayer) {
        return getProtectedRegion(plugin, player, warnPlayer, true);
    }

    public static @NotNull Optional<ProtectedRegion> getProtectedRegion(@NotNull BlockProtectionPlugin plugin,
                                                                        @NotNull Player player,
                                                                        boolean warnPlayer,
                                                                        boolean requireOwner) {
        LocalPlayer localPlayer = WorldGuardUtil.toLocalPlayer(player);
        BlockVector3 playerLocation = WorldGuardUtil.toBlockVector3(player.getLocation());
        Optional<RegionManager> regionManager = plugin.getRegionManager(player);
        if (regionManager.isEmpty()) {
            if (warnPlayer) {
                player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
            }
            return Optional.empty();
        }
        ApplicableRegionSet applicableRegions = regionManager.get().getApplicableRegions(playerLocation);
        // Cancel edit: does not have permission
        for (ProtectedRegion applicableRegion : applicableRegions) {
            if (applicableRegion.getType() == RegionType.GLOBAL) {
                continue;
            }
            if ((requireOwner && !applicableRegion.isOwner(localPlayer)) ||
                (!requireOwner && !applicableRegion.isMember(localPlayer))) {
                if (warnPlayer) {
                    player.sendMessage(plugin.getConfiguration().getNoRegionPermission());
                }
                return Optional.empty();
            }
            return Optional.of(applicableRegion);
        }
        if (warnPlayer) {
            player.sendMessage(plugin.getConfiguration().getRegionRequired());
        }
        return Optional.empty();
    }
}
