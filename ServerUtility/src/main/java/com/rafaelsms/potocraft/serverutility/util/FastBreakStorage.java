package com.rafaelsms.potocraft.serverutility.util;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class FastBreakStorage {

    public static final @NotNull BrokenBlockConsumer NO_OP_CONSUMER = (block, player) -> {
    };

    private final Map<Player, ItemStack> playerTools = Collections.synchronizedMap(new HashMap<>());
    private final Map<Player, Map<Location, Block>> playerBlocks = Collections.synchronizedMap(new HashMap<>());
    private final Map<Location, Block> blocks = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull BrokenBlockConsumer consumeOnBreak;
    private final int maxBreaksPerTick;

    protected FastBreakStorage(@NotNull BrokenBlockConsumer consumeOnBreak, int maxBreaksPerTick) {
        this.consumeOnBreak = consumeOnBreak;
        this.maxBreaksPerTick = maxBreaksPerTick;
    }

    public static FastBreakStorage build(@NotNull ServerUtilityPlugin plugin,
                                         @Nullable BrokenBlockConsumer consumer,
                                         int maxBreaksPerTick) {
        BrokenBlockConsumer blockConsumer = Optional.ofNullable(consumer).orElse(NO_OP_CONSUMER);
        if (plugin.isVulcanIntegrationAvailable()) {
            try {
                VulcanIntegrationConsumer integrationConsumer = new VulcanIntegrationConsumer(blockConsumer);
                plugin.getServer().getPluginManager().registerEvents(integrationConsumer, plugin);
                return new FastBreakStorage(integrationConsumer, maxBreaksPerTick);
            } catch (Throwable throwable) {
                plugin.logger()
                      .warn("Couldn't initialize Vulcan AntiCheat integration for quick break: {}",
                            throwable.getLocalizedMessage());
            }
        }
        return new FastBreakStorage(blockConsumer, maxBreaksPerTick);
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
                player.breakBlock(block);
                blockIterator.remove();
                consumeOnBreak.onBroken(block, player);

                // Stop if broke too many blocks this tick already
                processedBlocks++;
                if (processedBlocks >= maxBreaksPerTick) {
                    break;
                }
            }
        }
    }

    public interface BrokenBlockConsumer {
        void onBroken(@NotNull Block block, @NotNull Player player);
    }

}
