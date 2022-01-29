package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VillagerListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public VillagerListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void removeTreasureBooks(VillagerAcquireTradeEvent event) {
        if (!plugin.getConfiguration().isPreventingTreasureEnchantedBooks()) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        if (result.getType() != Material.ENCHANTED_BOOK) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void removeTreasureBooks(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Merchant merchant)) {
            return;
        }
        List<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());
        recipes.removeIf(recipe -> recipe.getResult().getType() == Material.ENCHANTED_BOOK);
        merchant.setRecipes(recipes);
    }
}
