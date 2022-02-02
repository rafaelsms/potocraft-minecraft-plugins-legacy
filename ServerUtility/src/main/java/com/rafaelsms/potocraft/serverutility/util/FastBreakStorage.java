package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FastBreakStorage {

    private static final int MAX_BREAKS_PER_TICK = 1;

    private final Map<Player, ItemStack> playerTools = Collections.synchronizedMap(new HashMap<>());
    private final Map<Player, Map<Location, Block>> playerBlocks = Collections.synchronizedMap(new HashMap<>());
    private final Map<Location, Block> blocks = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull Consumer<Block> consumeOnBreak;

    public FastBreakStorage() {
        this.consumeOnBreak = (block) -> {
        };
    }

    public FastBreakStorage(@NotNull Consumer<Block> consumeOnBreak) {
        this.consumeOnBreak = consumeOnBreak;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public void addBlocks(@NotNull Player player, @NotNull Map<Location, Block> blocks) {
        if (blocks.isEmpty()) {
            return;
        }
        Map<Location, Block> existingMap = playerBlocks.getOrDefault(player, new LinkedHashMap<>());
        existingMap.putAll(blocks);
        this.blocks.putAll(blocks);
        playerTools.put(player, player.getInventory().getItemInMainHand());
        playerBlocks.put(player, existingMap);
    }

    public boolean containsBlock(@NotNull Block block) {
        return blocks.containsKey(block.getLocation());
    }

    public void tickBreak() {
        Iterator<Map.Entry<Player, Map<Location, Block>>> iterator = playerBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, Map<Location, Block>> entry = iterator.next();
            Player player = entry.getKey();
            Map<Location, Block> playerBlocks = entry.getValue();

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
            Iterator<Block> blockIterator = playerBlocks.values().iterator();
            while (blockIterator.hasNext()) {
                Block block = blockIterator.next();
                ItemStack tool = playerTools.get(player);
                blocks.remove(block.getLocation());
                blockIterator.remove();
                block.breakNaturally(tool);
                consumeOnBreak.accept(block);

                // Stop if broke too many blocks this tick already
                processedBlocks++;
                if (processedBlocks > MAX_BREAKS_PER_TICK) {
                    break;
                }
            }
        }
    }

}
