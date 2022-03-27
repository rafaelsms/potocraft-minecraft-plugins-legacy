package com.rafaelsms.potocraft.serverutility.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PlayerLogging implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public PlayerLogging(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerElytraBoostEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "elytra boosted");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerTeleportEvent event) {
        logPlayer(event.getPlayer(),
                  event.getTo(),
                  "teleported by %s".formatted(event.getCause().name().toLowerCase()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerQuitEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "quit");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerPortalEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "entered portal");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerStartSpectatingEntityEvent event) {
        if (event.getNewSpectatorTarget() instanceof Player player) {
            logPlayer(event.getPlayer(), player.getLocation(), "spectating %s".formatted(player.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerStopSpectatingEntityEvent event) {
        if (event.getSpectatorTarget() instanceof Player player) {
            logPlayer(event.getPlayer(),
                      event.getPlayer().getLocation(),
                      "stopped spectating %s".formatted(player.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerDeathEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "died");
        // List player items with enchantments to console
        for (ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            Optional<String> enchantmentString = getEnchantmentString(itemStack);
            enchantmentString.ifPresent(s -> plugin.logger()
                                                   .info("Player had %d x %s %s".formatted(itemStack.getAmount(),
                                                                                           itemStack.getType().name(),
                                                                                           s)));
            if (Tag.SHULKER_BOXES.isTagged(itemStack.getType()) &&
                itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta &&
                blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                logContents(shulkerBox);
            }
        }
    }

    private void logPlayer(@NotNull Player player, @NotNull Location location, @Nullable String action) {
        if (plugin.getConfiguration().isPlayerLoggingEnabled()) {
            plugin.logger()
                  .info("%s %s at world = %s, %d %d %d".formatted(player.getName(),
                                                                  Util.getOrElse(action, ""),
                                                                  player.getWorld().getName(),
                                                                  location.getBlockX(),
                                                                  location.getBlockY(),
                                                                  location.getBlockZ()));
        }
    }

    private void logContents(@NotNull ShulkerBox shulkerBox) {
        for (ItemStack itemStack : shulkerBox.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            plugin.logger()
                  .info("Shulker box had %d x %s %s".formatted(itemStack.getAmount(),
                                                               itemStack.getType().getKey().getKey(),
                                                               getEnchantmentString(itemStack).orElse("")));
        }
    }

    private Optional<String> getEnchantmentString(@NotNull ItemStack itemStack) {
        if (!itemStack.getEnchantments().isEmpty()) {
            return Optional.of("enchanted with [ %s ]".formatted(getEnchantmentEnumeration(itemStack.getEnchantments()
                                                                                                    .entrySet())));
        }
        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorage) {
            return Optional.of("enchanted with [ %s ]".formatted(getEnchantmentEnumeration(enchantmentStorage.getStoredEnchants()
                                                                                                             .entrySet())));
        }
        return Optional.empty();
    }

    private String getEnchantmentEnumeration(Set<Map.Entry<Enchantment, Integer>> entries) {
        return TextUtil.joinStrings(entries,
                                    ", ",
                                    entry -> "%s %d".formatted(entry.getKey().getKey().getKey(), entry.getValue()));
    }
}
