package com.rafaelsms.potocraft.combatserver.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Equipment {

    private final @Nullable ItemStack helmet;
    private final @Nullable ItemStack chestplate;
    private final @Nullable ItemStack leggings;
    private final @Nullable ItemStack boots;

    private Equipment(@Nullable ItemStack helmet,
                      @Nullable ItemStack chestplate,
                      @Nullable ItemStack leggings,
                      @Nullable ItemStack boots) {
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public Equipment(@NotNull Material helmet,
                     @NotNull Material chestplate,
                     @NotNull Material leggings,
                     @NotNull Material boots,
                     int protectionLevel) {
        this(enchantProtection(helmet, protectionLevel),
             enchantProtection(chestplate, protectionLevel),
             enchantProtection(leggings, protectionLevel),
             enchantProtection(boots, protectionLevel));
    }

    public void apply(@NotNull Player player) {
        player.getInventory().setItem(EquipmentSlot.HEAD, helmet);
        player.getInventory().setItem(EquipmentSlot.CHEST, chestplate);
        player.getInventory().setItem(EquipmentSlot.LEGS, leggings);
        player.getInventory().setItem(EquipmentSlot.FEET, boots);
    }

    private static ItemStack enchantProtection(@NotNull Material material, int protectionLevel) {
        ItemStack itemStack = new ItemStack(material);
        if (protectionLevel > 0) {
            itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
        }
        return itemStack;
    }
}
