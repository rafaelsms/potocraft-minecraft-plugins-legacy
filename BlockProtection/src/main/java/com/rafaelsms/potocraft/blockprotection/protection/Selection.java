package com.rafaelsms.potocraft.blockprotection.protection;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.blockprotection.util.ProtectionException;
import com.rafaelsms.potocraft.blockprotection.util.ProtectionUtil;
import com.rafaelsms.potocraft.blockprotection.util.WorldGuardUtil;
import com.rafaelsms.potocraft.util.Util;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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

        // Find lowest and highest points
        Location lowestPoint = Util.getOrElse(this.lowestPoint, location.clone());
        Location highestPoint = Util.getOrElse(this.highestPoint, location.clone());
        int minYOffset = plugin.getConfiguration().getSelectionMinYOffset();
        int maxYOffset = plugin.getConfiguration().getSelectionMaxYOffset();
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        int minX = Math.min(lowestPoint.getBlockX(), location.getBlockX() - xzOffset);
        int maxX = Math.max(highestPoint.getBlockX(), location.getBlockX() + xzOffset);
        int minY = Math.min(lowestPoint.getBlockY(), location.getBlockY() - minYOffset);
        int maxY = Math.max(highestPoint.getBlockY(), location.getBlockY() + maxYOffset);
        int minZ = Math.min(lowestPoint.getBlockZ(), location.getBlockZ() - xzOffset);
        int maxZ = Math.max(highestPoint.getBlockZ(), location.getBlockZ() + xzOffset);
        // Make points
        lowestPoint = new Location(selectionWorld, minX, minY, minZ);
        highestPoint = new Location(selectionWorld, maxX, maxY, maxZ);

        // Check for overall maximum volume
        int selectionVolume = calculateVolume(lowestPoint, highestPoint);
        if (selectionVolume >= plugin.getConfiguration().getOverallMaximumVolume()) {
            return Result.MAX_VOLUME_EXCEEDED;
        }

        // Check for nearby permissions
        try {
            LocalPlayer player = WorldGuardUtil.toLocalPlayer(user.getPlayer());
            RegionManager regionManager = plugin.getRegionManagerInstance(selectionWorld);
            ProtectedCuboidRegion region = getTemporaryRegion(lowestPoint, highestPoint);
            ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(region);
            if (applicableRegions.size() > 0) {
                // Check if we are intersecting a prohibited region
                for (ProtectedRegion applicableRegion : applicableRegions) {
                    if (applicableRegion.getType() == RegionType.GLOBAL) {
                        continue;
                    }
                    if (!applicableRegion.isOwner(player)) {
                        return Result.NO_PERMISSION;
                    }
                    // Check if we are expanding a region
                    if (protectedRegion != null) {
                        // Block it if it is yet another region
                        if (!applicableRegion.getId().equalsIgnoreCase(protectedRegion.getId())) {
                            return Result.OTHER_REGION_FOUND;
                        }
                    } else {
                        // If not and we found another region, suggest expanding it
                        return Result.OTHER_REGION_FOUND;
                    }
                }
            }
        } catch (ProtectionException ignored) {
            return Result.FAILED_PROTECTION_FETCH;
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
        VOLUME_EXCEED_PERMISSION,
        NO_PERMISSION,
        ALLOWED;

        public boolean isSuccessful() {
            return this == ALLOWED;
        }

        public boolean shouldCancel() {
            return switch (this) {
                case WORLD_NOT_PROTECTED, MAX_VOLUME_EXCEEDED, FAILED_PROTECTION_FETCH, INVALID_LOCATION, NO_PERMISSION ->
                        true;
                default -> false;
            };
        }
    }
}
