package com.rafaelsms.potocraft.blockprotection.players;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class PlayerSelection {

    private final @NotNull BlockProtectionPlugin plugin;

    private final @NotNull World world;
    private @NotNull Location corner1;
    private @NotNull Location corner2;
    private int volume;

    PlayerSelection(@NotNull BlockProtectionPlugin plugin, @NotNull Location location) {
        this.plugin = plugin;
        this.world = location.getWorld();
        this.corner1 = getSelectionMin(location);
        this.corner2 = getSelectionMax(location);
        this.volume = getVolume(corner1, corner2);
    }

    public @NotNull UUID getWorldId() {
        return world.getUID();
    }

    public @NotNull Location getCorner1() {
        return corner1;
    }

    public @NotNull Location getCorner2() {
        return corner2;
    }

    public int getVolume() {
        return volume;
    }

    public boolean limitedSelect(@NotNull Location location, int maxVolume) {
        if (!location.getWorld().getUID().equals(world.getUID())) {
            throw new IllegalStateException("Different worlds on selection!");
        }
        Location selection1 = getSelectionMin(location);
        Location selection2 = getSelectionMax(location);
        Location newCorner1 = new Location(world,
                                           Math.min(selection1.getBlockX(), corner1.getBlockX()),
                                           Math.min(selection1.getBlockY(), corner1.getBlockY()),
                                           Math.min(selection1.getBlockZ(), corner1.getBlockZ()));
        Location newCorner2 = new Location(world,
                                           Math.max(selection2.getBlockX(), corner2.getBlockX()),
                                           Math.max(selection2.getBlockY(), corner2.getBlockY()),
                                           Math.max(selection2.getBlockZ(), corner2.getBlockZ()));
        int newVolume = getVolume(newCorner1, newCorner2);
        if (newVolume <= maxVolume) {
            this.corner1 = newCorner1;
            this.corner2 = newCorner2;
            this.volume = newVolume;
        }
        return false;
    }

    private int getVolume(@NotNull Location corner1, @NotNull Location corner2) {
        return (corner2.getBlockX() - corner1.getBlockX()) +
               (corner2.getBlockY() - corner1.getBlockY()) +
               (corner2.getBlockZ() - corner1.getBlockZ());
    }

    private Location getSelectionMin(@NotNull Location location) {
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        int minYOffset = plugin.getConfiguration().getSelectionMinYOffset();
        return location.clone().subtract(xzOffset, minYOffset, xzOffset);
    }

    private Location getSelectionMax(@NotNull Location location) {
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        int maxYOffset = plugin.getConfiguration().getSelectionMaxYOffset();
        return location.clone().add(xzOffset, maxYOffset, xzOffset);
    }
}
