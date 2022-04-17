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
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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
        Location to = event.getTo();
        logPlayer(event.getPlayer(),
                  event.getFrom(),
                  "teleported by %s to %s, %d %d %d from".formatted(event.getCause().name().toLowerCase(),
                                                                    to.getWorld().getName(),
                                                                    to.getBlockX(),
                                                                    to.getBlockY(),
                                                                    to.getBlockZ()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerJoinEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "joined");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerQuitEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "quit");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerPortalEvent event) {
        logPlayer(event.getPlayer(),
                  event.getFrom(),
                  "entered portal to %s from".formatted(parseLocation(event.getTo())));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerRespawnEvent event) {
        logPlayer(event.getPlayer(),
                  event.getRespawnLocation(),
                  "respawned with level %d".formatted(event.getPlayer().getLevel()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerItemBreakEvent event) {
        logPlayer(event.getPlayer(),
                  event.getPlayer().getLocation(),
                  "item %s %s broke".formatted(event.getBrokenItem().getType(),
                                               getEnchantmentString(event.getBrokenItem()).orElse("")));
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
        Player player = event.getPlayer();
        if (event.getKeepInventory()) {
            logPlayer(player, player.getLocation(), "died at level %d keeping inventory".formatted(player.getLevel()));
            return;
        }

        logPlayer(player,
                  player.getLocation(),
                  "died at level %d dropping %d items".formatted(player.getLevel(), event.getDrops().size()));
        // List player items with enchantments to console
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            Optional<String> enchantmentString = getEnchantmentString(itemStack);
            enchantmentString.ifPresent(s -> plugin.logger()
                                                   .info("{} had {} x {} {}",
                                                         player.getName(),
                                                         itemStack.getAmount(),
                                                         itemStack.getType().name(),
                                                         s));
            if (Tag.SHULKER_BOXES.isTagged(itemStack.getType()) &&
                itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta &&
                blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                logContents(player, shulkerBox);
            }
        }
    }

    private String parseLocation(@NotNull Location location) {
        return "world %s, %d %d %d".formatted(location.getWorld().getName(),
                                              location.getBlockX(),
                                              location.getBlockY(),
                                              location.getBlockZ());
    }

    private void logPlayer(@NotNull Player player, @NotNull Location printedLocation, @Nullable String action) {
        if (plugin.getConfiguration().isPlayerLoggingEnabled()) {
            plugin.logger()
                  .info("{} {} at {}", player.getName(), Util.getOrElse(action, ""), parseLocation(printedLocation));
        }
    }

    private void logContents(@NotNull Player player, @NotNull ShulkerBox shulkerBox) {
        for (ItemStack itemStack : shulkerBox.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            plugin.logger()
                  .info("Shulker box of {} had {} x {} {}",
                        player.getName(),
                        itemStack.getAmount(),
                        itemStack.getType().getKey().getKey(),
                        getEnchantmentString(itemStack).orElse(""));
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
