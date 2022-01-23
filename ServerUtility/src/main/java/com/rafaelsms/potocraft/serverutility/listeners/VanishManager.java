package com.rafaelsms.potocraft.serverutility.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VanishManager implements Listener {

    private final Map<UUID, Player> hiddenPlayers = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull ServerUtilityPlugin plugin;

    public VanishManager(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    public void toggleVanish(@NotNull Player player) {
        if (hiddenPlayers.containsKey(player.getUniqueId())) {
            showPlayer(player);
        } else {
            hidePlayer(player);
        }
    }

    public void hidePlayer(@NotNull Player player) {
        hiddenPlayers.put(player.getUniqueId(), player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                                                Integer.MAX_VALUE,
                                                1,
                                                false,
                                                false,
                                                true));
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(Permissions.COMMAND_VANISH_SEE_OTHERS)) {
                continue;
            }
            onlinePlayer.hidePlayer(plugin, player);
        }
    }

    public void showPlayer(@NotNull Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
    }

    public boolean isVanished(Player player) {
        return hiddenPlayers.containsKey(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void vanishOnJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(Permissions.COMMAND_VANISH_JOIN_VANISHED)) {
            return;
        }

        event.joinMessage(Component.empty());
        hidePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void vanishOnQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().hasPermission(Permissions.COMMAND_VANISH_QUIT_VANISHED)) {
            return;
        }

        event.quitMessage(Component.empty());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void hideOnJoin(PlayerJoinEvent event) {
        // Ignore if player can see hidden
        if (event.getPlayer().hasPermission(Permissions.COMMAND_VANISH_SEE_OTHERS)) {
            return;
        }

        for (Player hiddenPlayer : hiddenPlayers.values()) {
            event.getPlayer().hidePlayer(plugin, hiddenPlayer);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void removeOnQuit(PlayerQuitEvent event) {
        showPlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void preventInteractions(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }

        if (isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventInteractions(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (isVanished(player) && player.getGameMode() != GameMode.SPECTATOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventInteractions(PlayerPickupExperienceEvent event) {
        if (isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventInteractions(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventInteractions(PlayerInteractEvent event) {
        if (isVanished(event.getPlayer()) && event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventDamaged(PlayerDeathEvent event) {
        if (isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
