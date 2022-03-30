package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class BlockSearch {

    private static final BlockFace[] BLOCK_FACES;

    static {
        ArrayList<BlockFace> blockFaceArrayList = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    blockFaceArrayList.add(new BlockFace(dx, dy, dz));
                }
            }
        }
        BLOCK_FACES = blockFaceArrayList.toArray(new BlockFace[0]);
    }

    private final Set<Location> executedBlocks = new LinkedHashSet<>();
    private final Map<Location, Block> selectedBlocks = new LinkedHashMap<>();
    private final ArrayDeque<Block> blockQueue = new ArrayDeque<>();

    private final @NotNull Block startingBlock;
    private final @NotNull Predicate<Block> selectionPredicate;
    private final int maxBlocksSelected;

    public BlockSearch(@NotNull Block startingBlock,
                       @NotNull Predicate<Block> selectionPredicate,
                       int maxBlocksSelected) {
        this.startingBlock = startingBlock;
        this.selectionPredicate = selectionPredicate;
        this.maxBlocksSelected = maxBlocksSelected;
    }

    public static Map<Location, Block> searchBlocks(@NotNull Block block,
                                                    @NotNull Predicate<Block> selectionPredicate,
                                                    int maxBlocksSelected) {
        return new BlockSearch(block, selectionPredicate, maxBlocksSelected).search();
    }

    public Map<Location, Block> search() {
        blockQueue.add(startingBlock);
        while (!blockQueue.isEmpty() && selectedBlocks.size() < maxBlocksSelected) {
            Block block = blockQueue.pop();
            Location blockLocation = block.getLocation();
            // Execute block
            selectedBlocks.put(blockLocation, block);
            // Check if we should add more blocks
            for (BlockFace blockFace : BLOCK_FACES) {
                Block relative = block.getRelative(blockFace.modX(), blockFace.modY(), blockFace.modZ());
                Location relativeLocation = relative.getLocation();
                if (executedBlocks.contains(relativeLocation)) {
                    continue;
                }
                executedBlocks.add(relativeLocation);
                if (selectionPredicate.test(relative)) {
                    blockQueue.add(relative);
                }
            }
        }
        return selectedBlocks;
    }

    private record BlockFace(int modX, int modY, int modZ) {
    }
}
