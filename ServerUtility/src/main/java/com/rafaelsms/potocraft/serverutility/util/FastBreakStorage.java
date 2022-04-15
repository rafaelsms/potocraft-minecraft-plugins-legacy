package com.rafaelsms.potocraft.serverutility.util;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Object lock = new Object();
    private final Map<UUID, Integer> playerHeldItemSlot = new HashMap<>();
    private final Map<UUID, ItemStack> playerItemInHand = new HashMap<>();
    private final Map<Player, Map<Location, Block>> playerBlocks = new HashMap<>();
    private final Map<Location, Block> blocks = new HashMap<>();

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
        synchronized (lock) {
            return blocks.isEmpty();
        }
    }

    public void addBlocks(@NotNull Player player, @NotNull Map<Location, Block> blocks) {
        synchronized (lock) {
            if (blocks.isEmpty()) {
                return;
            }
            Map<Location, Block> existingMap = playerBlocks.getOrDefault(player, new LinkedHashMap<>());
            existingMap.putAll(blocks);
            this.blocks.putAll(blocks);
            playerHeldItemSlot.put(player.getUniqueId(), player.getInventory().getHeldItemSlot());
            playerItemInHand.put(player.getUniqueId(), player.getInventory().getItemInMainHand());
            playerBlocks.put(player, existingMap);
        }
    }

    public boolean containsBlock(@NotNull Block block) {
        synchronized (lock) {
            return blocks.containsKey(block.getLocation());
        }
    }

    public void tickBreak() {
        synchronized (lock) {
            Iterator<Map.Entry<Player, Map<Location, Block>>> iterator = playerBlocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Player, Map<Location, Block>> entry = iterator.next();
                Player player = entry.getKey();
                Map<Location, Block> playerBlocks = entry.getValue();

                // Check if player left
                if (!player.isOnline()) {
                    playerHeldItemSlot.remove(player.getUniqueId());
                    playerItemInHand.remove(player.getUniqueId());
                    iterator.remove();
                    continue;
                }

                if (playerBlocks.isEmpty()) {
                    playerHeldItemSlot.remove(player.getUniqueId());
                    playerItemInHand.remove(player.getUniqueId());
                    iterator.remove();
                    continue;
                }

                // Check if item in hand is the same for this tick. If it isn't, we will just remove all items
                boolean sameItemInHand =
                        playerHeldItemSlot.get(player.getUniqueId()) == player.getInventory().getHeldItemSlot() &&
                        Objects.equals(playerItemInHand.get(player.getUniqueId()),
                                       player.getInventory().getItemInMainHand());
                int processedBlocks = 0;
                Iterator<Block> blockIterator = playerBlocks.values().iterator();
                while (blockIterator.hasNext()) {
                    Block block = blockIterator.next();
                    blockIterator.remove();
                    blocks.remove(block.getLocation());
                    if (sameItemInHand) {
                        player.breakBlock(block);
                        consumeOnBreak.onBroken(block, player);
                    }

                    // Stop if broke too many blocks this tick already
                    processedBlocks++;
                    if (processedBlocks >= maxBreaksPerTick) {
                        break;
                    }
                }
            }
        }
    }

    public interface BrokenBlockConsumer {

        void onBroken(@NotNull Block block, @NotNull Player player);

    }
}
