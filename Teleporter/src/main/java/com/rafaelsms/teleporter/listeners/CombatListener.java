package com.rafaelsms.teleporter.listeners;

import com.rafaelsms.potocraft.plugin.combat.CombatType;
import com.rafaelsms.teleporter.TeleporterPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class CombatListener extends com.rafaelsms.potocraft.plugin.combat.CombatListener {

    private final @NotNull TeleporterPlugin plugin;

    public CombatListener(@NotNull TeleporterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void onPlayerVersusPlayerDamage(@NotNull Player damaged,
                                              @NotNull Player damager,
                                              @NotNull Entity damagerSource) {
        plugin.getUserManager().getUser(damaged).setCombatTicks(CombatType.PLAYER_COMBAT);
    }

    @Override
    protected void onPlayerDamage(@NotNull Player damaged, @NotNull CombatType combatType) {
        plugin.getUserManager().getUser(damaged).setCombatTicks(combatType);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void storeDeathLocation(PlayerDeathEvent event) {
        plugin.getUserManager()
              .getUser(event.getPlayer())
              .getProfile()
              .setDeathLocation(event.getPlayer().getLocation());
    }
}
