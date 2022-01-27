package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TotemListener implements Listener {

    private static final ItemStack totemOfUndying = new ItemStack(Material.TOTEM_OF_UNDYING);

    private final @NotNull ServerUtilityPlugin plugin;

    public TotemListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void preventMultipleTotems(PlayerJoinEvent event) {
        if (!plugin.getConfiguration().isSingleTotemOnly()) {
            return;
        }
        PlayerInventory inventory = event.getPlayer().getInventory();
        inventory.setContents(removeOtherTotems(event.getPlayer().getEyeLocation(), inventory.getContents()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void preventMultipleTotems(EntityPickupItemEvent event) {
        if (!plugin.getConfiguration().isSingleTotemOnly()) {
            return;
        }
        if (!event.getItem().getItemStack().isSimilar(totemOfUndying)) {
            return;
        }
        if (event.getEntity() instanceof Player player && player.getInventory().contains(totemOfUndying)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void preventMultipleTotems(InventoryClickEvent event) {
        if (!plugin.getConfiguration().isSingleTotemOnly()) {
            return;
        }
        HumanEntity entity = event.getView().getPlayer();
        PlayerInventory inventory = entity.getInventory();
        inventory.setContents(removeOtherTotems(entity.getEyeLocation(), inventory.getContents()));
        if (entity instanceof Player player) {
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void preventMultipleTotems(InventoryMoveItemEvent event) {
        if (!plugin.getConfiguration().isSingleTotemOnly()) {
            return;
        }
        Inventory inventory = event.getDestination();
        if (inventory.getHolder() instanceof Player player) {
            inventory.setContents(removeOtherTotems(player.getEyeLocation(), inventory.getContents()));
            player.updateInventory();
        }
    }

    private ItemStack[] removeOtherTotems(Location dropLocation, ItemStack[] contents) {
        boolean firstTotem = true;
        for (int i = 0; i < contents.length; i++) {
            ItemStack content = contents[i];
            if (content == null) {
                continue;
            }
            if (content.isSimilar(totemOfUndying)) {
                if (!firstTotem) {
                    dropLocation.getWorld().dropItem(dropLocation, content);
                    contents[i] = null;
                }
                firstTotem = false;
            }
        }
        return contents;
    }
}
