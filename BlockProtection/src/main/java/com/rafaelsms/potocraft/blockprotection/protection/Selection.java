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

    private int volumeCredit = 0;
    private @Nullable ProtectedRegion protectedRegion = null;

    private @Nullable World selectionWorld = null;
    private @Nullable Location lowestPoint = null;
    private @Nullable Location highestPoint = null;

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
        this.lowestPoint = BukkitAdapter.adapt(user.getPlayer().getWorld(), applicableRegion.getMinimumPoint());
        this.highestPoint = BukkitAdapter.adapt(user.getPlayer().getWorld(), applicableRegion.getMaximumPoint());
        this.volumeCredit = calculateVolume(lowestPoint, highestPoint);
    }

    public Optional<Boolean> hasOtherProtectedRegionsOnSelection() {
        // Make the worst
        if (selectionWorld == null || lowestPoint == null || highestPoint == null) {
            return Optional.empty();
        }
        try {
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
            this.lowestPoint = null;
            this.highestPoint = null;
        }
        return selectionResult;
    }

    private Result select(@NotNull Location location) {
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
            this.lowestPoint = null;
            this.highestPoint = null;
        }

        // Check if we can expand instead
        LocalPlayer player = WorldGuardUtil.toLocalPlayer(user.getPlayer());
        if (this.lowestPoint == null || this.highestPoint == null) {
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
                        this.lowestPoint = BukkitAdapter.adapt(location.getWorld(), protectedRegion.getMinimumPoint());
                        this.highestPoint = BukkitAdapter.adapt(location.getWorld(), protectedRegion.getMaximumPoint());
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
        Location currentLowestPoint = Util.getOrElse(this.lowestPoint, location.clone());
        Location currentHighestPoint = Util.getOrElse(this.highestPoint, location.clone());
        int minYOffset = plugin.getConfiguration().getSelectionMinYOffset();
        int maxYOffset = plugin.getConfiguration().getSelectionMaxYOffset();
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        Location selectionLowestPoint = location.clone().add(-xzOffset, -minYOffset, -xzOffset);
        Location selectionHighestPoint = location.clone().add(+xzOffset, maxYOffset, +xzOffset);
        // Make points
        Location lowestPoint = ProtectionUtil.getMinimumCoordinates(currentLowestPoint, selectionLowestPoint);
        Location highestPoint = ProtectionUtil.getMaximumCoordinates(currentHighestPoint, selectionHighestPoint);
        if (ProtectionUtil.isLocationHigher(lowestPoint, highestPoint)) {
            return Result.OTHER_REGION_WITHOUT_PERMISSION_FOUND;
        }

        // Check for nearby regions
        try {
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

                // Adapt selection around existing regions
                Location minPoint = BukkitAdapter.adapt(location.getWorld(), applicableRegion.getMinimumPoint());
                Location maxPoint = BukkitAdapter.adapt(location.getWorld(), applicableRegion.getMaximumPoint());
                boolean highestInside = ProtectionUtil.isLocationHigher(highestPoint, minPoint);
                boolean lowestInside = ProtectionUtil.isLocationHigher(maxPoint, lowestPoint);
                // If we are enclosing another region, abort
                if (highestInside && lowestInside) {
                    return Result.OTHER_REGION_WITH_PERMISSION_FOUND;
                } else if (highestInside) {
                    trimConflict(highestPoint, minPoint, false);
                } else if (lowestInside) {
                    trimConflict(lowestPoint, maxPoint, true);
                } else {
                    // How we got here?
                    return Result.FAILED_PROTECTION_DATA_FETCH;
                }

                // If this breaks our selection, restart
                if (ProtectionUtil.isLocationHigher(lowestPoint, highestPoint)) {
                    return Result.OTHER_REGION_WITH_PERMISSION_FOUND;
                }
            }
        } catch (ProtectionException ignored) {
            return Result.FAILED_PROTECTION_DATA_FETCH;
        }

        // Check for overall maximum volume
        int selectionVolume = calculateVolume(lowestPoint, highestPoint);
        if (selectionVolume >= plugin.getConfiguration().getOverallMaximumVolume()) {
            return Result.SELECTION_MAX_VOLUME_EXCEEDED;
        }

        // Check for player available volume
        if (!user.hasEnoughVolume(selectionVolume - volumeCredit)) {
            return Result.NOT_ENOUGH_VOLUME_ON_PROFILE;
        }

        // Update locations
        this.lowestPoint = lowestPoint;
        this.highestPoint = highestPoint;
        // Restart age because of interaction
        this.age = 0;
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
        if (lowestPoint == null || highestPoint == null) {
            return false;
        }
        int deltaX = Math.abs(highestPoint.getBlockX() - lowestPoint.getBlockX());
        int deltaZ = Math.abs(highestPoint.getBlockZ() - lowestPoint.getBlockZ());
        if (deltaX == 0 || deltaZ == 0) {
            return false;
        }
        double maxXZRatio = plugin.getConfiguration().getSelectionMaxXZRatio();
        double ratio = Math.max((double) deltaX / deltaZ, (double) deltaZ / deltaX);
        return ratio <= maxXZRatio;
    }

    public int getVolumeCost() {
        if (lowestPoint == null || highestPoint == null) {
            return 0;
        }
        return calculateVolume(lowestPoint, highestPoint) - volumeCredit;
    }

    public Optional<ProtectedRegion> getExistingProtectedRegion() {
        return Optional.ofNullable(protectedRegion);
    }

    public Optional<ProtectedRegion> getProtectedRegion(@NotNull String id, boolean isTransient) {
        if (lowestPoint == null || highestPoint == null) {
            return Optional.empty();
        }
        return Optional.of(WorldGuardUtil.getRegion(id, isTransient, lowestPoint, highestPoint));
    }

    private static ProtectedCuboidRegion getTemporaryRegion(@NotNull Location lowestPoint,
                                                            @NotNull Location highestPoint) {
        return WorldGuardUtil.getRegion("temporaryRegion", true, lowestPoint, highestPoint);
    }

    public void updateStatus() {
        if (selectionWorld == null || lowestPoint == null || highestPoint == null) {
            return;
        }
        if ((age % plugin.getConfiguration().getParticlePeriodTicks()) == 0) {
            ProtectionUtil.drawCuboid(user.getPlayer(), lowestPoint, highestPoint);
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

    private int calculateVolume(@NotNull Location location1, @NotNull Location location2) {
        return Math.abs(location1.getBlockX() - location2.getBlockX()) *
               Math.abs(location1.getBlockY() - location2.getBlockY()) *
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
