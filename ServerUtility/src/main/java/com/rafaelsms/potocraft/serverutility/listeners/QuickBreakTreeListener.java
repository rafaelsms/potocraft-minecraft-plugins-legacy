package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.BlockSearch;
import com.rafaelsms.potocraft.serverutility.util.FastBreakStorage;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class QuickBreakTreeListener implements Listener {

    // Max height is 30 according to wiki
    private static final int MAX_SEARCH_DISTANCE = 33;
    private static final int MAX_MANHATTAN_DISTANCE = 2;

    private final Set<TreeMaterial> treeMaterials = Set.of(TreeMaterial.of(Material.OAK_LOG, Material.OAK_LEAVES),
                                                           TreeMaterial.of(Material.DARK_OAK_LOG,
                                                                           Material.DARK_OAK_LEAVES),
                                                           TreeMaterial.of(Material.SPRUCE_LOG, Material.SPRUCE_LEAVES),
                                                           TreeMaterial.of(Material.BIRCH_LOG, Material.BIRCH_LEAVES),
                                                           TreeMaterial.of(Material.ACACIA_LOG, Material.ACACIA_LEAVES),
                                                           TreeMaterial.of(Material.JUNGLE_LOG,
                                                                           Material.JUNGLE_LEAVES));

    private final @NotNull ServerUtilityPlugin plugin;
    private final FastBreakStorage fastBreakStorage = new FastBreakStorage();

    public QuickBreakTreeListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void registerTasks(ServerLoadEvent event) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, fastBreakStorage::tickBreak, 1, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void quickBreakTree(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        if (!player.hasPermission(Permissions.QUICK_BREAK_TREES)) {
            return;
        }
        Block block = event.getBlock();
        if (fastBreakStorage.containsBlock(block)) {
            return;
        }

        Optional<Map<Location, Block>> optionalTree = getTree(block);
        if (optionalTree.isEmpty()) {
            return;
        }
        fastBreakStorage.addBlocks(player, optionalTree.get());
    }

    private Optional<Map<Location, Block>> getTree(@NotNull Block block) {
        for (TreeMaterial treeMaterial : treeMaterials) {
            if (!treeMaterial.isTreeLog(block.getType())) {
                continue;
            }
            Map<Location, Block> logs = BlockSearch.searchBlocks(block,
                                                                 new TreeLogPredicate(treeMaterial,
                                                                                      block.getLocation(),
                                                                                      MAX_MANHATTAN_DISTANCE,
                                                                                      MAX_SEARCH_DISTANCE));
            Map<Location, Block> leaves = BlockSearch.searchBlocks(block,
                                                                   new TreePredicate(treeMaterial,
                                                                                     logs,
                                                                                     block.getLocation(),
                                                                                     MAX_MANHATTAN_DISTANCE,
                                                                                     MAX_SEARCH_DISTANCE));
            logs.putAll(leaves);
            return Optional.of(logs);
        }
        return Optional.empty();
    }

    private record TreeLogPredicate(@NotNull TreeMaterial treeMaterial,
                                    @NotNull Location startingLocation,
                                    int xzDistance,
                                    int yDistance) implements Predicate<Block> {

        @Override
        public boolean test(Block block) {
            // Select just connected logs, ignoring xz distance
            return treeMaterial.isTreeLog(block.getType()) &&
                   Math.abs(block.getY() - startingLocation.getBlockY()) <= yDistance;
        }
    }

    private record TreePredicate(@NotNull TreeMaterial treeMaterial,
                                 @NotNull Map<Location, Block> logs,
                                 @NotNull Location startingLocation,
                                 int xzDistance,
                                 int yDistance) implements Predicate<Block> {

        @Override
        public boolean test(Block block) {
            // Select existing logs and surrounding leaves
            return logs.containsKey(block.getLocation()) ||
                   (treeMaterial.isTreeLeave(block.getType()) &&
                    Math.abs(block.getY() - startingLocation.getBlockY()) <= yDistance &&
                    Util.getManhattanDistance(startingLocation, block.getLocation()) <= xzDistance);
        }
    }

    private record TreeMaterial(@NotNull Material log, @NotNull Material leaves) {

        public static @NotNull TreeMaterial of(@NotNull Material log, @NotNull Material leaves) {
            return new TreeMaterial(log, leaves);
        }

        public boolean isTreeLog(@NotNull Material material) {
            return material == log;
        }

        public boolean isTreeLeave(@NotNull Material material) {
            return material == leaves;
        }

        public boolean isTreeBlock(@NotNull Material material) {
            return isTreeLeave(material) || isTreeLog(material);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TreeMaterial that = (TreeMaterial) o;
            return log == that.log && leaves == that.leaves;
        }

        @Override
        public int hashCode() {
            return Objects.hash(log, leaves);
        }
    }
}
