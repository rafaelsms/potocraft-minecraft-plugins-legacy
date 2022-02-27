package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
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

    // TODO give equipment on join
    // TODO change equipment on upgrade
}
