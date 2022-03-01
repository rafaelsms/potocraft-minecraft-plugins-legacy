package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class HeadDropper implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public HeadDropper(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void dropHeadOnPVP(PlayerDeathEvent event) {
        // If there is no player killer responsible, ignore
        if (event.getPlayer().getKiller() == null) {
            return;
        }
        if (!plugin.getConfiguration().isPlayerHeadDropping()) {
            return;
        }
        // Ignore if KeepInventory or doEntityDrops
        if (Boolean.TRUE.equals(event.getPlayer().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
            return;
        }
        if (Boolean.FALSE.equals(event.getPlayer().getWorld().getGameRuleValue(GameRule.DO_ENTITY_DROPS))) {
            return;
        }
        // Drop player head alongside all items
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta itemMeta = (SkullMeta) playerHead.getItemMeta();
        itemMeta.setOwningPlayer(event.getPlayer());
        itemMeta.displayName(event.getPlayer().displayName());
        itemMeta.lore(plugin.getConfiguration().getPlayerHeadLore(event.getPlayer(), event.getPlayer().getKiller()));
        if (playerHead.setItemMeta(itemMeta)) {
            event.getDrops().add(playerHead);
        }
    }
}
