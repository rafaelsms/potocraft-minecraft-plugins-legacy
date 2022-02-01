package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

public class BlockSearch {

    private final Set<Block> executedBlocks = new LinkedHashSet<>();
    private final Set<Block> selectedBlocks = new LinkedHashSet<>();
    private final ArrayDeque<Block> blockQueue = new ArrayDeque<>();

    private final @NotNull Block startingBlock;
    private final @NotNull Predicate<Block> selectionPredicate;

    public BlockSearch(@NotNull Block startingBlock, @NotNull Predicate<Block> selectionPredicate) {
        this.startingBlock = startingBlock;
        this.selectionPredicate = selectionPredicate;
    }

    public static Set<Block> searchBlocks(@NotNull Block block, @NotNull Predicate<Block> selectionPredicate) {
        return new BlockSearch(block, selectionPredicate).search();
    }

    public Set<Block> search() {
        blockQueue.add(startingBlock);
        while (!blockQueue.isEmpty()) {
            Block block = blockQueue.pop();
            // Execute block
            selectedBlocks.add(block);
            // Check if we should add more blocks
            for (BlockFace blockFace : BlockFace.values()) {
                Block relative = block.getRelative(blockFace);
                if (executedBlocks.contains(relative)) {
                    continue;
                }
                executedBlocks.add(block);
                if (selectionPredicate.test(relative)) {
                    blockQueue.add(relative);
                }
            }
        }
        return selectedBlocks;
    }
}
