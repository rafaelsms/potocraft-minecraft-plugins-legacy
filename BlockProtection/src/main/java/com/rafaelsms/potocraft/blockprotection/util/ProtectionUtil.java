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
        if (lowestPoint.getWorld() == null || highestPoint.getWorld() == null) {
            throw new IllegalArgumentException("Locations must have a defined world.");
        }
        if (!Objects.equals(lowestPoint.getWorld().getUID(), highestPoint.getWorld().getUID())) {
            throw new IllegalArgumentException("Cuboid between different worlds.");
        }

        Location location = lowestPoint.clone();
        int startX = lowestPoint.getBlockX();
        int endX = highestPoint.getBlockX();
        int startY = lowestPoint.getBlockY();
        int endY = highestPoint.getBlockY();
        int startZ = lowestPoint.getBlockZ();
        int endZ = highestPoint.getBlockZ();

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

}
