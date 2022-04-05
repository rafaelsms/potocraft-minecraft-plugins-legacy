package com.rafaelsms.potocraft.plugin.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public abstract class CombatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    protected void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }
        onPlayerDamage(damaged, CombatType.getFromDamageSource(event.getCause()));
    }

    @EventHandler(ignoreCancelled = true)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }

        Player damager;
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        } else if (event.getDamager() instanceof Player player) {
            damager = player;
        } else {
            return;
        }

        onPlayerVersusPlayerDamage(damaged, damager, event.getDamager());
    }

    protected abstract void onPlayerVersusPlayerDamage(@NotNull Player damaged,
                                                       @NotNull Player damager,
                                                       @NotNull Entity damagerSource);

    protected abstract void onPlayerDamage(@NotNull Player damaged, @NotNull CombatType combatType);
}
