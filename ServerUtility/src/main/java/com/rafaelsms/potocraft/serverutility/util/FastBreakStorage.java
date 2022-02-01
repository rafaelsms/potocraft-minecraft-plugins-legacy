package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class FastBreakStorage {

    private static final int MAX_BREAKS_PER_TICK = 3;

    private final Map<Player, Set<Block>> playerBlocks = Collections.synchronizedMap(new HashMap<>());
    private final Set<Block> blocks = Collections.synchronizedSet(new HashSet<>());

    public void addBlocks(@NotNull Player player, @NotNull Set<Block> blocks) {
        Set<Block> existingSet = playerBlocks.getOrDefault(player, new LinkedHashSet<>());
        existingSet.addAll(blocks);
        this.blocks.addAll(blocks);
        playerBlocks.put(player, existingSet);
    }

    public boolean containsBlock(@NotNull Block block) {
        return blocks.contains(block);
    }

    public void tickBreak() {
        Iterator<Map.Entry<Player, Set<Block>>> iterator = playerBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, Set<Block>> entry = iterator.next();
            Player player = entry.getKey();
            Set<Block> playerBlocks = entry.getValue();

            // Check if player left
            if (!player.isOnline()) {
                iterator.remove();
                continue;
            }

            if (playerBlocks.isEmpty()) {
                iterator.remove();
                continue;
            }

            int processedBlocks = 0;
            Iterator<Block> blockIterator = playerBlocks.iterator();
            while (blockIterator.hasNext()) {
                Block block = blockIterator.next();
                block.breakNaturally(true);
                blocks.remove(block);
                blockIterator.remove();

                // Stop if broke too many blocks this tick already
                processedBlocks++;
                if (processedBlocks > MAX_BREAKS_PER_TICK) {
                    break;
                }
            }
        }
    }

}
