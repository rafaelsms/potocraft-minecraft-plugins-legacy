package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class DamageModifier implements Listener {

    private final Set<Enchantment> protectionEnchantments = Set.of(Enchantment.PROTECTION_FIRE,
                                                                   Enchantment.PROTECTION_ENVIRONMENTAL,
                                                                   Enchantment.PROTECTION_PROJECTILE,
                                                                   Enchantment.PROTECTION_EXPLOSIONS);
    private final @NotNull ServerUtilityPlugin plugin;

    public DamageModifier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void applyOverallDamageModifier(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Apply our damage multiplier
        event.setDamage(event.getDamage() * plugin.getConfiguration().getOverallDamageDealtMultiplier());
        // Apply our armor multiplier
        double armorTypeFinalModifier = getArmorTypeFinalModifier(player.getInventory());
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR,
                        event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) * armorTypeFinalModifier);
        double enchantmentFinalModifier = getProtectionEnchantmentFinalModifier(player.getInventory());
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC,
                        event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) * enchantmentFinalModifier);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void applyPlayerVersusPlayerModifier(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = null;
        if (event.getDamager() instanceof Player player) {
            damager = player;
        } else if (event.getDamager() instanceof Projectile projectile &&
                   projectile.getShooter() instanceof Player player) {
            damager = player;
        }
        if (damager == null) {
            return;
        }

        event.setDamage(event.getDamage() * plugin.getConfiguration().getPlayerVersusPlayerDamageMultiplier());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void applyProjectileDamageMultiplier(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        event.setDamage(event.getDamage() * plugin.getConfiguration().getArrowDamageMultiplier());
    }

    @EventHandler
    private void applyCooldownModifier(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        double factor = plugin.getConfiguration().getCooldownDamageFactor();
        float attackCooldown = player.getAttackCooldown();
        double damageMultiplier = Math.max((attackCooldown - factor) / (1.0 - factor), 0.0);
        event.setDamage(event.getDamage() * damageMultiplier);
    }

    private double getArmorTypeFinalModifier(@NotNull PlayerInventory inventory) {
        double damageModifier = 1.0;
        for (ItemStack itemStack : inventory.getArmorContents()) {
            if (itemStack == null) {
                continue;
            }
            damageModifier *= switch (itemStack.getType()) {
                case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS ->
                        plugin.getConfiguration().getLeatherArmorModifier();
                case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS ->
                        plugin.getConfiguration().getIronChainArmorModifier();
                case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS ->
                        plugin.getConfiguration().getIronArmorModifier();
                case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS ->
                        plugin.getConfiguration().getGoldArmorModifier();
                case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS ->
                        plugin.getConfiguration().getDiamondArmorModifier();
                case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS ->
                        plugin.getConfiguration().getNetheriteArmorModifier();
                default -> 1.0;
            };
        }
        return damageModifier;
    }

    private double getProtectionEnchantmentFinalModifier(@NotNull PlayerInventory inventory) {
        double damageModifier = 1.0;
        List<Double> enchantmentArmorMultiplier = plugin.getConfiguration().getProtectionEnchantmentArmorMultiplier();
        for (ItemStack itemStack : inventory.getArmorContents()) {
            if (itemStack == null) {
                continue;
            }

            for (Enchantment enchantment : protectionEnchantments) {
                int enchantmentLevel = itemStack.getEnchantmentLevel(enchantment);
                if (enchantmentLevel > 0 && enchantmentLevel <= enchantmentArmorMultiplier.size()) {
                    damageModifier *= enchantmentArmorMultiplier.get(enchantmentLevel - 1);
                }
            }
        }
        return damageModifier;
    }
}
