package com.rafaelsms.potocraft.blockprotection.protection;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.blockprotection.util.ProtectionException;
import com.rafaelsms.potocraft.blockprotection.util.ProtectionUtil;
import com.rafaelsms.potocraft.blockprotection.util.WorldGuardUtil;
import com.rafaelsms.potocraft.util.Util;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class Selection implements Runnable {

    private final @NotNull BlockProtectionPlugin plugin;
    private final @NotNull User user;

    private final int timeToLive;
    private int age = 0;

    private int areaCredit = 0;
    private @Nullable ProtectedRegion protectedRegion = null;

    private @Nullable World selectionWorld = null;
    private @Nullable Location lowCorner = null; // lower on XZ axis, base Y level
    private @Nullable Location highCorner = null; // higher on XZ axis, highest Y possible

    public Selection(@NotNull BlockProtectionPlugin plugin, @NotNull User user) {
        this.plugin = plugin;
        this.user = user;
        this.timeToLive = plugin.getConfiguration().getSelectionTimeToLive();
    }

    public Selection(@NotNull BlockProtectionPlugin plugin,
                     @NotNull User user,
                     @NotNull ProtectedRegion applicableRegion) {
        this(plugin, user);
        this.protectedRegion = applicableRegion;
        this.selectionWorld = user.getPlayer().getWorld();
        this.lowCorner = BukkitAdapter.adapt(user.getPlayer().getWorld(), applicableRegion.getMinimumPoint());
        this.highCorner = BukkitAdapter.adapt(user.getPlayer().getWorld(), applicableRegion.getMaximumPoint());
        this.areaCredit = calculateArea(lowCorner, highCorner);
    }

    public Optional<Boolean> hasOtherProtectedRegionsOnSelection() {
        // Make the worst
        if (selectionWorld == null || lowCorner == null || highCorner == null) {
            return Optional.empty();
        }
        try {
            RegionManager regionManager = plugin.getRegionManagerInstance(selectionWorld);
            ProtectedCuboidRegion region = getTemporaryRegion(lowCorner, highCorner);
            ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(region);
            // Check if we are intersecting a prohibited region
            for (ProtectedRegion applicableRegion : applicableRegions) {
                if (applicableRegion.getType() == RegionType.GLOBAL) {
                    continue;
                }
                // Ignore if we are editing this region
                if (protectedRegion != null && applicableRegion.getId().equalsIgnoreCase(protectedRegion.getId())) {
                    continue;
                }
                return Optional.of(true);
            }
        } catch (ProtectionException e) {
            return Optional.empty();
        }
        return Optional.of(false);
    }

    public Result makeSelection(@NotNull Location location) {
        Result selectionResult = select(location);
        if (selectionResult.shouldCancelSelection()) {
            this.lowCorner = null;
            this.highCorner = null;
        }
        return selectionResult;
    }

    private Result select(@NotNull Location location) {
        // Restart age because of interaction
        this.age = 0;

        // Check for selection world
        if (location.getWorld() == null) {
            return Result.INVALID_LOCATION_WORLD;
        }

        // If changed world, reset selection
        if (selectionWorld == null || !Objects.equals(location.getWorld().getUID(), selectionWorld.getUID())) {
            if (!plugin.getConfiguration().getProtectedWorlds().contains(location.getWorld().getName().toLowerCase())) {
                return Result.WORLD_IS_NOT_PROTECTED;
            }

            this.selectionWorld = location.getWorld();
            assert this.selectionWorld != null;
            this.lowCorner = null;
            this.highCorner = null;
        }

        // Check if we can expand instead
        LocalPlayer player = WorldGuardUtil.toLocalPlayer(user.getPlayer());
        if (this.lowCorner == null || this.highCorner == null) {
            try {
                RegionManager regionManager = plugin.getRegionManagerInstance(selectionWorld);
                BlockVector3 position = WorldGuardUtil.toBlockVector3(location);
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(position);
                for (ProtectedRegion applicableRegion : applicableRegions) {
                    if (applicableRegion.getType() == RegionType.GLOBAL) {
                        continue;
                    }
                    if (!applicableRegion.isOwner(player)) {
                        return Result.OTHER_REGION_WITHOUT_PERMISSION_FOUND;
                    } else if (protectedRegion == null) {
                        // Select current
                        this.protectedRegion = applicableRegion;
                        this.lowCorner = BukkitAdapter.adapt(location.getWorld(), protectedRegion.getMinimumPoint());
                        this.highCorner = BukkitAdapter.adapt(location.getWorld(), protectedRegion.getMaximumPoint());
                        this.areaCredit = calculateArea(lowCorner, highCorner);
                    } else {
                        // If we are expanding and already found another region, something is wrong
                        return Result.OTHER_REGION_WITH_PERMISSION_FOUND;
                    }
                }
            } catch (ProtectionException ignored) {
                return Result.FAILED_PROTECTION_DATA_FETCH;
            }
        }

        // Find lowest and highest points
        Location currentLowestPoint = Util.getOrElse(this.lowCorner, location.clone());
        Location currentHighestPoint = Util.getOrElse(this.highCorner, location.clone());
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        Location selectionLowestPoint = location.clone().add(-xzOffset, 0, -xzOffset);
        selectionLowestPoint.setY(plugin.getConfiguration().getSelectionMinYProtection());
        Location selectionHighestPoint = location.clone().add(+xzOffset, 0, +xzOffset);
        selectionHighestPoint.setY(selectionWorld.getMaxHeight());
        // Make points
        Location lowestPoint = ProtectionUtil.getMinimumCoordinates(currentLowestPoint, selectionLowestPoint);
        Location highestPoint = ProtectionUtil.getMaximumCoordinates(currentHighestPoint, selectionHighestPoint);
        if (ProtectionUtil.isLocationHigher(lowestPoint, highestPoint)) {
            return Result.OTHER_REGION_WITHOUT_PERMISSION_FOUND;
        }

        // Check for nearby regions
        try {
            boolean checkApplicableRegions = true;
            while (checkApplicableRegions) {
                checkApplicableRegions = false;
                RegionManager regionManager = plugin.getRegionManagerInstance(selectionWorld);
                ProtectedCuboidRegion region = getTemporaryRegion(lowestPoint, highestPoint);
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(region);
                // Check if we are intersecting a prohibited region
                for (ProtectedRegion applicableRegion : applicableRegions) {
                    if (applicableRegion.getType() == RegionType.GLOBAL) {
                        continue;
                    }
                    // Ignore if we are editing this region
                    if (protectedRegion != null && applicableRegion.getId().equalsIgnoreCase(protectedRegion.getId())) {
                        continue;
                    }
                    // Abort if another player's selection is found
                    if (!applicableRegion.isOwner(player)) {
                        return Result.OTHER_REGION_WITHOUT_PERMISSION_FOUND;
                    }
                    // If there is no protected region, we can expand this (as applicableRegion has player as owner)
                    if (protectedRegion == null) {
                        // Expand it
                        this.protectedRegion = applicableRegion;
                        Location minPoint = BukkitAdapter.adapt(selectionWorld, applicableRegion.getMinimumPoint());
                        Location maxPoint = BukkitAdapter.adapt(selectionWorld, applicableRegion.getMaximumPoint());
                        this.areaCredit = calculateArea(minPoint, maxPoint);
                        lowestPoint = ProtectionUtil.getMinimumCoordinates(lowestPoint, minPoint);
                        highestPoint = ProtectionUtil.getMaximumCoordinates(highestPoint, maxPoint);
                        // Recheck for existing regions inside it
                        checkApplicableRegions = true;
                        continue;
                    }
                    return Result.OTHER_REGION_WITH_PERMISSION_FOUND;
                }
            }

            // If this breaks our selection, restart
            if (ProtectionUtil.isLocationHigher(lowestPoint, highestPoint)) {
                return Result.OTHER_REGION_WITH_PERMISSION_FOUND;
            }
        } catch (ProtectionException ignored) {
            return Result.FAILED_PROTECTION_DATA_FETCH;
        }

        // Check for overall maximum volume
        int selectionArea = calculateArea(lowestPoint, highestPoint);
        if (selectionArea >= plugin.getConfiguration().getOverallMaximumArea()) {
            return Result.SELECTION_MAX_VOLUME_EXCEEDED;
        }

        // Check for player available volume
        if (!user.hasEnoughArea(selectionArea - areaCredit)) {
            return Result.NOT_ENOUGH_VOLUME_ON_PROFILE;
        }

        // Update locations
        this.lowCorner = lowestPoint;
        this.highCorner = highestPoint;
        return Result.SELECTION_ALLOWED;
    }

    private void trimConflict(@NotNull Location selectionPoint, @NotNull Location regionPoint, boolean trimUpwards) {
        int delta = trimUpwards ? +1 : -1;
        // We must trim either on X or on Z axis
        // Make a minimal trim (try to remove a minimal portion of the selection)
        int deltaX = Math.abs(regionPoint.getBlockX() - selectionPoint.getBlockX());
        int deltaZ = Math.abs(regionPoint.getBlockZ() - selectionPoint.getBlockZ());
        if (deltaX >= deltaZ) {
            // Since x-axis is greater, we will trim Z to the neighbor of the minimum point
            selectionPoint.setZ(regionPoint.getBlockZ() + delta);
        } else {
            // Since x-axis is greater, we will trim Z to the neighbor of the minimum point
            selectionPoint.setX(regionPoint.getBlockX() + delta);
        }
    }

    public boolean testMaximumXZRatio() {
        if (lowCorner == null || highCorner == null) {
            return false;
        }
        int deltaX = Math.abs(lowCorner.getBlockX() - highCorner.getBlockX());
        int deltaZ = Math.abs(lowCorner.getBlockZ() - highCorner.getBlockZ());
        if (deltaX == 0 || deltaZ == 0) {
            return false;
        }
        double maxXZRatio = plugin.getConfiguration().getSelectionMaxXZRatio();
        double ratio = Math.max((double) deltaX / deltaZ, (double) deltaZ / deltaX);
        return ratio <= maxXZRatio;
    }

    public int getAreaCost() {
        if (lowCorner == null || highCorner == null) {
            return 0;
        }
        return calculateArea(lowCorner, highCorner) - areaCredit;
    }

    public Optional<ProtectedRegion> getExistingProtectedRegion() {
        return Optional.ofNullable(protectedRegion);
    }

    public Optional<ProtectedRegion> getProtectedRegion(@NotNull String id, boolean isTransient) {
        if (lowCorner == null || highCorner == null) {
            return Optional.empty();
        }
        return Optional.of(WorldGuardUtil.getRegion(id, isTransient, lowCorner, highCorner));
    }

    private static ProtectedCuboidRegion getTemporaryRegion(@NotNull Location lowestPoint,
                                                            @NotNull Location highestPoint) {
        return WorldGuardUtil.getRegion("temporaryRegion", true, lowestPoint, highestPoint);
    }

    public void updateStatus() {
        if (selectionWorld == null || lowCorner == null || highCorner == null) {
            return;
        }
        if ((age % plugin.getConfiguration().getParticlePeriodTicks()) == 0) {
            ProtectionUtil.drawCuboid(user.getPlayer(), lowCorner, highCorner);
        }
    }

    public Optional<World> getSelectionWorld() {
        return Optional.ofNullable(selectionWorld);
    }

    @Override
    public void run() {
        age++;
        if (age >= timeToLive) {
            user.setSelection(null);
            return;
        }
        updateStatus();
    }

    private int calculateArea(@NotNull Location location1, @NotNull Location location2) {
        return Math.abs(location1.getBlockX() - location2.getBlockX()) *
               Math.abs(location1.getBlockZ() - location2.getBlockZ());
    }

    public enum Result {

        // Fatal errors
        WORLD_IS_NOT_PROTECTED,
        INVALID_LOCATION_WORLD,
        FAILED_PROTECTION_DATA_FETCH,
        OTHER_REGION_WITH_PERMISSION_FOUND,
        OTHER_REGION_WITHOUT_PERMISSION_FOUND,
        SELECTION_MAX_VOLUME_EXCEEDED,
        // Warnings
        NOT_ENOUGH_VOLUME_ON_PROFILE,
        // Successes
        SELECTION_ALLOWED;

        public boolean selectionSucceeded() {
            return this == SELECTION_ALLOWED;
        }

        public boolean shouldCancelSelection() {
            return !selectionSucceeded() && this != NOT_ENOUGH_VOLUME_ON_PROFILE;
        }
    }
}
