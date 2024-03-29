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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class FastBreakStorage {

    public static final @NotNull BrokenBlockConsumer NO_OP_CONSUMER = (block, player) -> {
    };

    private final Map<UUID, Integer> playerHeldSlot = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, ItemStack> playerItemHeld = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Player> playerInstances = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Map<Location, Block>> playerBlocks = Collections.synchronizedMap(new HashMap<>());
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
            // Wrap around for Vulcan
            blockConsumer = registerIntegration(plugin, c -> {
                try {
                    return Optional.of(new VulcanIntegrationConsumer(c));
                } catch (Throwable exception) {
                    plugin.logger().warn("Couldn't initialize Vulcan Integration for quick break:", exception);
                    return Optional.empty();
                }
            }, blockConsumer);
        }
        if (plugin.isLACIntegrationAvailable()) {
            // Wrap around for LAC
            blockConsumer = registerIntegration(plugin, c -> {
                try {
                    return Optional.of(new LightAntiCheatIntegrationConsumer(c));
                } catch (Throwable exception) {
                    plugin.logger().warn("Couldn't initialize LAC Integration for quick break:", exception);
                    return Optional.empty();
                }
            }, blockConsumer);
        }
        return new FastBreakStorage(blockConsumer, maxBreaksPerTick);
    }

    private static BrokenBlockConsumer registerIntegration(@NotNull ServerUtilityPlugin plugin,
                                                           @NotNull Function<BrokenBlockConsumer, Optional<AntiCheatIntegration>> integrationSupplier,
                                                           @Nullable BrokenBlockConsumer consumer) {
        AntiCheatIntegration integration = integrationSupplier.apply(consumer).orElseThrow();
        plugin.getServer().getPluginManager().registerEvents(integration, plugin);
        return integration;
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
        UUID playerId = player.getUniqueId();
        playerHeldSlot.put(playerId, player.getInventory().getHeldItemSlot());
        playerItemHeld.put(playerId, player.getInventory().getItemInMainHand());
        playerInstances.put(playerId, player);
        playerBlocks.put(playerId, existingMap);
    }

    public boolean containsBlock(@NotNull Block block) {
        return blocks.containsKey(block.getLocation());
    }

    public void tickBreak() {
        Iterator<Map.Entry<UUID, Map<Location, Block>>> iterator = playerBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Map<Location, Block>> entry = iterator.next();
            UUID playerId = entry.getKey();
            Player player = playerInstances.get(playerId);
            Map<Location, Block> playerBlocks = entry.getValue();

            // Check if player left, there are no more blocks or if player changed item in hand
            if (player == null ||
                !player.isOnline() ||
                playerBlocks.isEmpty() ||
                !Objects.equals(playerItemHeld.get(playerId), player.getInventory().getItemInMainHand()) ||
                !Objects.equals(playerHeldSlot.get(playerId), player.getInventory().getHeldItemSlot())) {
                playerHeldSlot.remove(playerId);
                playerItemHeld.remove(playerId);
                playerInstances.remove(playerId);
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
