package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class DamageModifier implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public DamageModifier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void changeCooldownModifier(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        double factor = plugin.getConfiguration().getCooldownDamageFactor();
        float attackCooldown = player.getAttackCooldown();
        double damageMultiplier = Math.max((attackCooldown - factor) / (1.0 - factor), 0.0);
        event.setDamage(event.getDamage() * damageMultiplier);
    }
}
