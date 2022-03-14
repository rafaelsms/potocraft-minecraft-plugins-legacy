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
            if (applicableRegions.size() > 0) {
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
            }
        } catch (ProtectionException e) {
            return Optional.empty();
        }
        return Optional.of(false);
    }

    public Result select(@NotNull Location location) {
        // Check for selection world
        if (location.getWorld() == null) {
            return Result.INVALID_LOCATION;
        }

        // If changed world, reset selection
        if (selectionWorld == null || !Objects.equals(location.getWorld().getUID(), selectionWorld.getUID())) {
            if (!plugin.getConfiguration().getProtectedWorlds().contains(location.getWorld().getName())) {
                return Result.WORLD_NOT_PROTECTED;
            }

            this.selectionWorld = location.getWorld();
            assert this.selectionWorld != null;
            this.lowestPoint = null;
            this.highestPoint = null;
        }

        // Check if we can expand instead
        try {
            if (this.lowestPoint == null || this.highestPoint == null) {
                LocalPlayer player = WorldGuardUtil.toLocalPlayer(user.getPlayer());
                RegionManager regionManager = plugin.getRegionManagerInstance(selectionWorld);
                BlockVector3 position = WorldGuardUtil.toBlockVector3(location);
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(position);
                if (applicableRegions.size() > 0) {
                    for (ProtectedRegion applicableRegion : applicableRegions) {
                        if (applicableRegion.getType() == RegionType.GLOBAL) {
                            continue;
                        }
                        if (!applicableRegion.isOwner(player)) {
                            return Result.NO_PERMISSION;
                        } else if (protectedRegion == null) {
                            // Select current
                            this.protectedRegion = applicableRegion;
                            this.lowestPoint =
                                    BukkitAdapter.adapt(location.getWorld(), protectedRegion.getMinimumPoint());
                            this.highestPoint =
                                    BukkitAdapter.adapt(location.getWorld(), protectedRegion.getMaximumPoint());
                        } else {
                            // If we are expanding and already found another region, something is wrong
                            return Result.OTHER_REGION_FOUND;
                        }
                    }
                }
            }
        } catch (ProtectionException ignored) {
            return Result.FAILED_PROTECTION_FETCH;
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
            return Result.NO_PERMISSION;
        }

        // Check for nearby regions
        try {
            RegionManager regionManager = plugin.getRegionManagerInstance(selectionWorld);
            ProtectedCuboidRegion region = getTemporaryRegion(lowestPoint, highestPoint);
            ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(region);
            if (applicableRegions.size() > 0) {
                // Check if we are intersecting a prohibited region
                for (ProtectedRegion applicableRegion : applicableRegions) {
                    if (applicableRegion.getType() == RegionType.GLOBAL) {
                        continue;
                    }
                    // Ignore if we are editing this region
                    if (protectedRegion != null && applicableRegion.getId().equalsIgnoreCase(protectedRegion.getId())) {
                        continue;
                    }
                    // TODO fix this logic (relevant screenshot)
                    // Adapt selection around existing regions
                    Location minPoint = BukkitAdapter.adapt(location.getWorld(), applicableRegion.getMinimumPoint());
                    Location maxPoint = BukkitAdapter.adapt(location.getWorld(), applicableRegion.getMaximumPoint());
                    boolean highestIsHigher = ProtectionUtil.isLocationHigher(highestPoint, maxPoint);
                    boolean lowestIsLower = ProtectionUtil.isLocationHigher(lowestPoint, minPoint);
                    lowestPoint = ProtectionUtil.getMaximumCoordinates(minPoint.add(+1, +1, +1), lowestPoint);
                    highestPoint = ProtectionUtil.getMinimumCoordinates(minPoint.add(-1, 0, -1), highestPoint);
                    // If this breaks our selection, restart
                    if (ProtectionUtil.isLocationHigher(lowestPoint, highestPoint)) {
                        return Result.OTHER_REGION_FOUND;
                    }
                }
            }
        } catch (ProtectionException ignored) {
            return Result.FAILED_PROTECTION_FETCH;
        }

        // Check for overall maximum volume
        int selectionVolume = calculateVolume(lowestPoint, highestPoint);
        if (selectionVolume >= plugin.getConfiguration().getOverallMaximumVolume()) {
            return Result.MAX_VOLUME_EXCEEDED;
        }

        // Check for player available volume
        if (!user.hasEnoughVolume(selectionVolume - volumeCredit)) {
            return Result.NOT_ENOUGH_VOLUME;
        }

        // Update locations
        this.lowestPoint = lowestPoint;
        this.highestPoint = highestPoint;
        // Restart age because of interaction
        this.age = 0;
        return Result.ALLOWED;
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

        WORLD_NOT_PROTECTED,
        INVALID_LOCATION,
        FAILED_PROTECTION_FETCH,
        OTHER_REGION_FOUND,
        MAX_VOLUME_EXCEEDED,
        NOT_ENOUGH_VOLUME,
        NO_PERMISSION,
        ALLOWED;

        public boolean isSuccessful() {
            return this == ALLOWED;
        }

        public boolean shouldCancel() {
            return !isSuccessful();
        }
    }
}
