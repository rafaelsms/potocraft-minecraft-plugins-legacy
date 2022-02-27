package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DamageModifier implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public DamageModifier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void applyOverallDamageModifier(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Apply our damage multiplier
        event.setDamage(event.getDamage() * plugin.getConfiguration().getOverallDamageDealtMultiplier());
        // Apply our armor multiplier
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR,
                        event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) *
                        plugin.getConfiguration().getArmorDamageReductionMultiplier());
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC,
                        event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) *
                        plugin.getConfiguration().getEnchantmentDamageReductionMultiplier());
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
}
