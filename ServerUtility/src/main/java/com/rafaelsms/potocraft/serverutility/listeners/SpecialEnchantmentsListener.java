package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpecialEnchantmentsListener implements Listener {

    private static final ArrayList<Enchantment> treasureEnchantments = new ArrayList<>();
    private static final ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);

    static {
        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment.isTreasure()) {
                treasureEnchantments.add(enchantment);
            }
        }
    }

    private final @NotNull ServerUtilityPlugin plugin;

    public SpecialEnchantmentsListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void removeSpecialEnchantments(VillagerAcquireTradeEvent event) {
        if (!plugin.getConfiguration().isRemoveSpecialEnchantmentsFromVillagers()) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        for (Enchantment treasureEnchantment : treasureEnchantments) {
            if (result.containsEnchantment(treasureEnchantment)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void removeSpecialEnchantments(PlayerInteractEntityEvent event) {
        if (!plugin.getConfiguration().isRemoveSpecialEnchantmentsFromVillagers()) {
            return;
        }
        if (!(event.getRightClicked() instanceof Merchant villager)) {
            return;
        }
        List<MerchantRecipe> recipeList = new ArrayList<>(villager.getRecipes());
        for (MerchantRecipe recipe : recipeList) {
            ItemStack itemStack = recipe.getResult();
            if (!itemStack.isSimilar(enchantedBook)) {
                continue;
            }
            EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
            for (Enchantment treasureEnchantment : treasureEnchantments) {
                if (itemMeta.removeStoredEnchant(treasureEnchantment)) {
                    recipe.setMaxUses(Integer.MIN_VALUE);
                    recipe.setUses(Integer.MAX_VALUE);
                    break;
                }
            }
        }
        villager.setRecipes(recipeList);
    }

    @EventHandler
    private void removeSpecialEnchantments(InventoryOpenEvent event) {
        if (!plugin.getConfiguration().isRemoveSpecialEnchantmentsFromInventory()) {
            return;
        }
        Inventory inventory = event.getInventory();
        inventory.setContents(removeSpecialEnchantments(inventory.getContents()));
    }

    @EventHandler
    private void removeSpecialEnchantments(InventoryClickEvent event) {
        if (!plugin.getConfiguration().isRemoveSpecialEnchantmentsFromInventory()) {
            return;
        }
        Inventory inventory = event.getInventory();
        inventory.setContents(removeSpecialEnchantments(inventory.getContents()));
        event.setCancelled(true);
    }

    @EventHandler
    private void removeSpecialEnchantments(PlayerJoinEvent event) {
        if (!plugin.getConfiguration().isRemoveSpecialEnchantmentsFromInventory()) {
            return;
        }
        PlayerInventory inventory = event.getPlayer().getInventory();
        inventory.setContents(removeSpecialEnchantments(inventory.getContents()));
    }

    private ItemStack[] removeSpecialEnchantments(ItemStack[] contents) {
        for (ItemStack content : contents) {
            if (content == null) {
                continue;
            }
            if (!content.getEnchantments().isEmpty()) {
                for (Enchantment treasureEnchantment : treasureEnchantments) {
                    content.removeEnchantment(treasureEnchantment);
                }
            }
            if (content.isSimilar(enchantedBook)) {
                EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) content.getItemMeta();
                for (Enchantment treasureEnchantment : treasureEnchantments) {
                    itemMeta.removeStoredEnchant(treasureEnchantment);
                }
                content.setItemMeta(itemMeta);
            }
        }
        return contents;
    }
}
