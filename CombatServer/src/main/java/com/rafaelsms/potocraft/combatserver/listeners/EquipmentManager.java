package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EquipmentManager implements Listener {

    private final @NotNull CombatServerPlugin plugin;

    public EquipmentManager(@NotNull CombatServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void preventEquipmentDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void preventItemAmountDecreaseOnConsumption(PlayerItemConsumeEvent event) {
        ItemStack itemStack = Optional.ofNullable(event.getReplacement()).orElse(event.getItem());
        itemStack.setAmount(Math.min(itemStack.getAmount(), 64));
        event.setReplacement(itemStack);
    }

    @EventHandler(ignoreCancelled = true)
    private void preventItemAmountDecreaseOnConsumption(EntityShootBowEvent event) {
        event.setConsumeItem(false);
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void setArmorStandArms(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) {
            return;
        }
        armorStand.setArms(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void preventItemDrop(PlayerDropItemEvent event) {
        // If player is on creative, allow
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventArmorStandItemFrameDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand armorStand) {
            if (event.getDamager() instanceof Player player) {
                // Allow creative players to bypass it all
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return;
                }
                getArmorFromStand(player, armorStand);
            }
            event.setCancelled(true);
        } else if (event.getEntity() instanceof ItemFrame itemFrame) {
            if (event.getDamager() instanceof Player player) {
                // Allow creative players to bypass it all
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return;
                }
                getItemFromFrame(player, itemFrame);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void getEquipmentFromStand(PlayerArmorStandManipulateEvent event) {
        // If player is on creative, allow interaction
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Don't interact but get items
        getArmorFromStand(event.getPlayer(), event.getRightClicked());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void getItemFromItemFrame(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) {
            return;
        }
        // If player is on creative, allow interaction
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Give item to player
        event.getPlayer().getInventory().addItem(itemFrame.getItem());
        event.setCancelled(true);
    }

    private void getItemFromFrame(@NotNull Player player, @NotNull ItemFrame itemFrame) {
        player.getInventory().addItem(itemFrame.getItem());
    }

    private void getArmorFromStand(@NotNull Player player, @NotNull ArmorStand armorStand) {
        PlayerInventory inventory = player.getInventory();
        // Clear inventory
        inventory.clear();
        // Set armor contents
        inventory.setHelmet(armorStand.getEquipment().getHelmet());
        inventory.setChestplate(armorStand.getEquipment().getChestplate());
        inventory.setLeggings(armorStand.getEquipment().getLeggings());
        inventory.setBoots(armorStand.getEquipment().getBoots());
        // Set main and offhand
        inventory.setItemInMainHand(armorStand.getItem(EquipmentSlot.HAND));
        inventory.setItemInOffHand(armorStand.getItem(EquipmentSlot.OFF_HAND));
    }

    // TODO give equipment on login based on upgrades
    // TODO change equipment on upgrade
}
