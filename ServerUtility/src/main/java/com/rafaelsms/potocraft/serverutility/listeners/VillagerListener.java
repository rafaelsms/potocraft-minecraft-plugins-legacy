package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VillagerListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public VillagerListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void removeTreasureBooks(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();
        if (result.getType() != Material.ENCHANTED_BOOK) {
            return;
        }
        if (plugin.getConfiguration().isPreventingAllEnchantedBooks()) {
            event.setCancelled(true);
            return;
        }
        if (plugin.getConfiguration().isPreventingTreasureEnchantedBooks() && hasTreasureEnchantment(result)) {
            event.setCancelled(true);
            return;
        }
        if (plugin.getConfiguration().isNerfVillagerEnchantedBooks()) {
            // Attempt to nerf the item
            Optional<ItemStack> optional = nerfEnchantments(result);
            if (optional.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            // Copy all recipe but nerf the item
            // God, please just add a MerchantRecipe#setResult
            MerchantRecipe newMerchantRecipe = new MerchantRecipe(optional.get(),
                                                                  recipe.getUses(),
                                                                  recipe.getMaxUses(),
                                                                  recipe.hasExperienceReward(),
                                                                  recipe.getVillagerExperience(),
                                                                  recipe.getPriceMultiplier(),
                                                                  recipe.getDemand(),
                                                                  recipe.getSpecialPrice(),
                                                                  recipe.shouldIgnoreDiscounts());
            newMerchantRecipe.setIngredients(recipe.getIngredients());
            event.setRecipe(newMerchantRecipe);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void removeTreasureBooks(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Merchant merchant)) {
            return;
        }
        List<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());

        // Change recipes based on configuration
        if (plugin.getConfiguration().isPreventingAllEnchantedBooks()) {
            recipes.removeIf(recipe -> recipe.getResult().getType() == Material.ENCHANTED_BOOK);
            merchant.setRecipes(recipes);
            // This option overrides the other two, return
            return;
        }

        // These two options can co-exist, apply separately
        if (plugin.getConfiguration().isPreventingTreasureEnchantedBooks()) {
            recipes.removeIf(recipe -> hasTreasureEnchantment(recipe.getResult()));
        }
        if (plugin.getConfiguration().isNerfVillagerEnchantedBooks()) {
            List<MerchantRecipe> recipesToAdd = new ArrayList<>();
            Iterator<MerchantRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                MerchantRecipe recipe = iterator.next();
                ItemStack result = recipe.getResult();
                if (result.getType() != Material.ENCHANTED_BOOK) {
                    continue;
                }
                // Attempt to nerf the item
                Optional<ItemStack> optional = nerfEnchantments(result);
                iterator.remove();
                // Remove it since we can't replace it
                if (optional.isEmpty()) {
                    continue;
                }
                // Otherwise, add a new recipe but with nerfed enchantments
                MerchantRecipe newMerchantRecipe = new MerchantRecipe(optional.get(),
                                                                      recipe.getUses(),
                                                                      recipe.getMaxUses(),
                                                                      recipe.hasExperienceReward(),
                                                                      recipe.getVillagerExperience(),
                                                                      recipe.getPriceMultiplier(),
                                                                      recipe.getDemand(),
                                                                      recipe.getSpecialPrice(),
                                                                      recipe.shouldIgnoreDiscounts());
                newMerchantRecipe.setIngredients(recipe.getIngredients());
                recipesToAdd.add(newMerchantRecipe);
            }
            recipes.addAll(recipesToAdd);
        }

        // Finally, set recipes
        merchant.setRecipes(recipes);
    }

    private boolean hasTreasureEnchantment(@NotNull ItemStack itemStack) {
        if (itemStack.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }
        EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
        for (Enchantment enchantment : itemMeta.getStoredEnchants().keySet()) {
            if (enchantment.isTreasure()) {
                return true;
            }
        }
        return false;
    }

    private @NotNull Optional<ItemStack> nerfEnchantments(@NotNull ItemStack itemStack) {
        if (itemStack.getType() != Material.ENCHANTED_BOOK) {
            return Optional.of(itemStack);
        }
        EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
        HashMap<Enchantment, Integer> map = new HashMap<>(itemMeta.getStoredEnchants());
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int maxLevel = enchantment.getMaxLevel();
            if (maxLevel <= 2) {
                itemMeta.removeStoredEnchant(enchantment);
                continue;
            }
            int level = entry.getValue();
            while (maxLevel - level <= 2 && level > 1) {
                // The difference is too low, decrease its level
                level = level - 1;
            }
            itemMeta.removeStoredEnchant(enchantment);
            itemMeta.addStoredEnchant(enchantment, Math.max(1, level), false);
        }
        if (itemMeta.getStoredEnchants().isEmpty()) {
            return Optional.empty();
        }
        itemStack.setItemMeta(itemMeta);
        return Optional.of(itemStack);
    }
}
