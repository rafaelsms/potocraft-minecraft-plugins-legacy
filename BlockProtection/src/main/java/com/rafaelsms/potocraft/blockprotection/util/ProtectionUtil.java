package com.rafaelsms.potocraft.blockprotection.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ProtectionUtil {

    // Private constructor
    private ProtectionUtil() {
    }

    public static void drawCuboid(@NotNull Player player,
                                  @NotNull Location lowestPoint,
                                  @NotNull Location highestPoint) {
        assertSameWorld(lowestPoint, highestPoint);
        Location location = lowestPoint.clone();
        // +1 will draw the particle right on the edge of the selection (since the entire block is being selected)
        int startX = lowestPoint.getBlockX();
        int endX = highestPoint.getBlockX() + 1;
        int startY = lowestPoint.getBlockY();
        int endY = highestPoint.getBlockY() + 1;
        int startZ = lowestPoint.getBlockZ();
        int endZ = highestPoint.getBlockZ() + 1;

        // For every X, we draw 4 lines (at each yz corner)
        for (int dx = 0; dx <= (endX - startX); dx++) {
            spawnParticle(player, location.set(startX + dx, startY, startZ));
            spawnParticle(player, location.set(startX + dx, startY, endZ));
            spawnParticle(player, location.set(startX + dx, endY, startZ));
            spawnParticle(player, location.set(startX + dx, endY, endZ));
        }
        // For every Y, we draw 4 lines (at each xz corner)
        for (int dy = 0; dy <= (endY - startY); dy++) {
            spawnParticle(player, location.set(startX, startY + dy, startZ));
            spawnParticle(player, location.set(startX, startY + dy, endZ));
            spawnParticle(player, location.set(endX, startY + dy, startZ));
            spawnParticle(player, location.set(endX, startY + dy, endZ));
        }
        // For every Z, we draw 4 lines (at each xy corner)
        for (int dz = 0; dz <= (endZ - startZ); dz++) {
            spawnParticle(player, location.set(startX, startY, startZ + dz));
            spawnParticle(player, location.set(startX, endY, startZ + dz));
            spawnParticle(player, location.set(endX, startY, startZ + dz));
            spawnParticle(player, location.set(endX, endY, startZ + dz));
        }
    }

    private static void spawnParticle(@NotNull Player player, @NotNull Location location) {
        player.spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
    }

    public static @NotNull Location getMinimumCoordinates(@NotNull Location location1, @NotNull Location location2) {
        assertSameWorld(location1, location2);
        return new Location(location1.getWorld(),
                            Math.min(location1.getBlockX(), location2.getBlockX()),
                            Math.min(location1.getBlockY(), location2.getBlockY()),
                            Math.min(location1.getBlockZ(), location2.getBlockZ()));
    }

    public static @NotNull Location getMaximumCoordinates(@NotNull Location location1, @NotNull Location location2) {
        assertSameWorld(location1, location2);
        return new Location(location1.getWorld(),
                            Math.max(location1.getBlockX(), location2.getBlockX()),
                            Math.max(location1.getBlockY(), location2.getBlockY()),
                            Math.max(location1.getBlockZ(), location2.getBlockZ()));
    }

    public static boolean isLocationHigher(@NotNull Location supposedlyLower, @NotNull Location supposedlyHigher) {
        assertSameWorld(supposedlyLower, supposedlyHigher);
        return supposedlyLower.getBlockX() >= supposedlyHigher.getBlockX() ||
               supposedlyLower.getBlockY() >= supposedlyHigher.getBlockY() ||
               supposedlyLower.getBlockZ() >= supposedlyHigher.getBlockZ();
    }

    private static void assertSameWorld(@NotNull Location location1, @NotNull Location location2) {
        if (location1.getWorld() == null || location2.getWorld() == null) {
            throw new IllegalArgumentException("Locations must have a defined world.");
        }
        if (!Objects.equals(location1.getWorld().getUID(), location2.getWorld().getUID())) {
            throw new IllegalArgumentException("Cuboid between different worlds.");
        }
    }

    public static boolean isSameWorld(@NotNull Location location1, @NotNull Location location2) {
        return location1.getWorld() != null &&
               location2.getWorld() != null &&
               Objects.equals(location1.getWorld().getUID(), location2.getWorld().getUID());
    }
}
