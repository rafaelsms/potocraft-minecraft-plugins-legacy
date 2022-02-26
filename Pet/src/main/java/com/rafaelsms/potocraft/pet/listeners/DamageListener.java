package com.rafaelsms.potocraft.pet.listeners;

import com.rafaelsms.potocraft.pet.PetPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DamageListener implements Listener {

    private final @NotNull PetPlugin plugin;

    public DamageListener(@NotNull PetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void setPlayerLastDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.getUserManager().getUser(player).setDamageTicks();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void setPlayerLastDamage(EntityDamageByEntityEvent event) {
        Player damager = null;
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        } else if (event.getDamager() instanceof Player player) {
            damager = player;
        }

        if (damager != null && event.getEntity() instanceof LivingEntity) {
            plugin.getUserManager().getUser(damager).setDamageTicks();
        }
    }
}
