package com.rafaelsms.potocraft.blockprotection.players;

import com.google.common.collect.Sets;
import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BlockChangeCache {

    private final @NotNull BlockProtectionPlugin plugin;
    private final @NotNull User user;

    private Set<BlockChange> sentBlockChanges = new HashSet<>();
    private Set<BlockChange> blockChanges = new HashSet<>();

    public BlockChangeCache(@NotNull BlockProtectionPlugin plugin, @NotNull User user) {
        this.plugin = plugin;
        this.user = user;
    }

    public synchronized void showRectangle(@NotNull Location corner1,
                                           @NotNull Location corner2,
                                           @NotNull BlockData blockData) {
        Player player = user.getPlayer();
        // Retrieve data
        World world = corner1.getWorld();
        if (!corner2.getWorld().getUID().equals(world.getUID())) {
            player.sendMultiBlockChange(getBlockChangeMap());
            return;
        }
        int startX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int endX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int startY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int endY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int startZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int endZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        player.sendMessage("Volume: %d".formatted((endX - startX) * (endY - startY) * (endZ - startZ)));

        // For every X, we draw 4 lines (at each yz corner)
        for (int dx = 0; dx <= (endX - startX); dx++) {
            insertBlock(new Location(world, startX + dx, startY, startZ), blockData);
            insertBlock(new Location(world, startX + dx, startY, endZ), blockData);
            insertBlock(new Location(world, startX + dx, endY, startZ), blockData);
            insertBlock(new Location(world, startX + dx, endY, endZ), blockData);
        }
        // For every Y, we draw 4 lines (at each xz corner)
        for (int dy = 0; dy <= (endY - startY); dy++) {
            insertBlock(new Location(world, startX, startY + dy, startZ), blockData);
            insertBlock(new Location(world, startX, startY + dy, endZ), blockData);
            insertBlock(new Location(world, endX, startY + dy, startZ), blockData);
            insertBlock(new Location(world, endX, startY + dy, endZ), blockData);
        }
        // For every Z, we draw 4 lines (at each xy corner)
        for (int dz = 0; dz <= (endZ - startZ); dz++) {
            insertBlock(new Location(world, startX, startY, startZ + dz), blockData);
            insertBlock(new Location(world, startX, endY, startZ + dz), blockData);
            insertBlock(new Location(world, endX, startY, startZ + dz), blockData);
            insertBlock(new Location(world, endX, endY, startZ + dz), blockData);
        }

        // Show changes
        player.sendMultiBlockChange(getBlockChangeMap());
    }

    private @NotNull Map<Location, BlockData> getBlockChangeMap() {
        Map<Location, BlockData> changeMap = new HashMap<>();
        // The diference between new set and already sent is blocks who are not yet sent to the player
        for (BlockChange blockChange : Sets.difference(blockChanges, sentBlockChanges)) {
            changeMap.put(blockChange.location(), blockChange.sent());
        }
        // The difference between already sent and the new set is blocks that must be reverted to the original state
        for (BlockChange blockChange : Sets.difference(sentBlockChanges, blockChanges)) {
            changeMap.put(blockChange.location(), blockChange.original());
        }
        // Change variables and start a new set for the next change
        sentBlockChanges = blockChanges;
        blockChanges = new HashSet<>();
        return changeMap;
    }

    private void insertBlock(@NotNull Location position, @NotNull BlockData sentData) {
        blockChanges.add(new BlockChange(position, position.getBlock().getBlockData().clone(), sentData));
    }

    public synchronized void clearBlocks() {
        user.getPlayer().sendMultiBlockChange(getBlockChangeMap());
    }

    private record BlockChange(@NotNull Location location, @NotNull BlockData original, @NotNull BlockData sent) {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BlockChange that = (BlockChange) o;
            return location.equals(that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location);
        }
    }
}
