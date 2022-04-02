package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.rafaelsms.potocraft.serverprofile.players.tasks.CombatTask;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public class CombatListener implements Listener {

    private final Set<EntityDamageEvent.DamageCause> combatDamageCauses = Set.of(EntityDamageEvent.DamageCause.LAVA,
                                                                                 EntityDamageEvent.DamageCause.MELTING,
                                                                                 EntityDamageEvent.DamageCause.PROJECTILE,
                                                                                 EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                                                                                 EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                                                                                 EntityDamageEvent.DamageCause.DRAGON_BREATH,
                                                                                 EntityDamageEvent.DamageCause.FALLING_BLOCK,
                                                                                 EntityDamageEvent.DamageCause.FIRE,
                                                                                 EntityDamageEvent.DamageCause.FIRE_TICK,
                                                                                 EntityDamageEvent.DamageCause.POISON,
                                                                                 EntityDamageEvent.DamageCause.THORNS);

    private final @NotNull ServerProfilePlugin plugin;

    public CombatListener(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void cancelCommandInCombat(PlayerCommandPreprocessEvent event) {
        User user = plugin.getUserManager().getUser(event.getPlayer());
        if (!user.isInCombat()) {
            return;
        }

        String[] args = event.getMessage().toLowerCase().split(" ");
        if (args.length <= 0) {
            event.setCancelled(true);
            return;
        }

        String command = args[0].replaceFirst("/", "");
        for (String blockedCommand : plugin.getConfiguration().getCombatBlockedCommands()) {
            if (command.equalsIgnoreCase(blockedCommand)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getConfiguration().getCombatBlockedCommand());
                return;
            }
            PluginCommand pluginCommand = plugin.getServer().getPluginCommand(blockedCommand);
            if (pluginCommand == null) {
                plugin.logger().warn("Command not found to be blocked: \"{}\"", blockedCommand);
                continue;
            }
            for (String alias : pluginCommand.getAliases()) {
                if (command.equalsIgnoreCase(alias)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(plugin.getConfiguration().getCombatBlockedCommand());
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void storeDeathLocation(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getUserManager().getUser(player);
        Location location = event.getEntity().getLocation();
        user.getProfile().setDeathLocation(location);
        user.getProfile().setBackLocation(location);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void showDeathLocation(PlayerRespawnEvent event) {
        // Ignore if player used end portal
        for (PlayerRespawnEvent.RespawnFlag respawnFlag : event.getRespawnFlags()) {
            if (respawnFlag == PlayerRespawnEvent.RespawnFlag.END_PORTAL) {
                return;
            }
        }
        Player player = event.getPlayer();
        if (!player.hasPermission(Permissions.COMBAT_SHOW_DEATH_LOCATION)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player);
        Optional<Location> locationOptional = user.getProfile().getDeathLocation(plugin);
        if (locationOptional.isEmpty()) {
            return;
        }
        player.sendMessage(plugin.getConfiguration().getCombatDeathLocation(locationOptional.get()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void killInCombat(PlayerQuitEvent event) {
        User user = plugin.getUserManager().getUser(event.getPlayer());
        if (!user.isInCombat()) {
            return;
        }
        if (plugin.getConfiguration().isCombatLogOffDestroysTotemFirst()) {
            event.getPlayer().damage(Double.MAX_VALUE, event.getPlayer());
        } else {
            event.getPlayer().setHealth(0.0);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void setOutOfCombat(PlayerDeathEvent event) {
        User user = plugin.getUserManager().getUser(event.getPlayer());
        if (user.isInCombat()) {
            plugin.logger()
                  .info("Player {} ({}) died in combat.", event.getPlayer().getName(), event.getPlayer().getUniqueId());
        }
        user.clearCombatTask();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void setOutOfCombat(PlayerKickEvent event) {
        plugin.getUserManager().getUser(event.getPlayer()).clearCombatTask();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void setInCombat(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        if (!(damaged instanceof Player playerDamaged)) {
            return;
        }

        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }

        User userDamaged = plugin.getUserManager().getUser(playerDamaged);
        if (damager instanceof Player playerDamager) {
            int combatDuration = plugin.getConfiguration().getPlayerCombatDurationTicks();
            User userDamager = plugin.getUserManager().getUser(playerDamager);
            userDamager.setCombatTask(CombatTask.Type.PLAYER, combatDuration);
            userDamaged.setCombatTask(CombatTask.Type.PLAYER, combatDuration);
            userDamaged.setKiller(playerDamager);
        } else {
            int combatDuration = plugin.getConfiguration().getMobCombatDurationTicks();
            userDamaged.setCombatTask(CombatTask.Type.MOB, combatDuration);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void setInCombat(EntityDamageEvent event) {
        Entity damaged = event.getEntity();
        if (!(damaged instanceof Player playerDamaged)) {
            return;
        }
        if (combatDamageCauses.contains(event.getCause())) {
            int combatDuration = plugin.getConfiguration().getMobCombatDurationTicks();
            plugin.getUserManager().getUser(playerDamaged).setCombatTask(CombatTask.Type.MOB, combatDuration);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void assertPlayerKiller(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.getUserManager().getUser(player).getKiller().ifPresent(event.getEntity()::setKiller);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void keepInventoryOutsideCombat(PlayerDeathEvent event) {
        // If we are dropping items, leave it as is
        if (plugin.getConfiguration().isOutOfCombatDeathDroppingItems()) {
            return;
        }
        // If player is in combat, leave it as is
        if (plugin.getUserManager().getUser(event.getPlayer()).isInCombat()) {
            return;
        }
        // Otherwise, keep inventory and drop nothing
        event.setKeepInventory(true);
        event.getDrops().clear();
    }

    @EventHandler(ignoreCancelled = true)
    private void keepExperienceOutsideCombat(PlayerDeathEvent event) {
        // If we are dropping experience, leave it as is
        if (plugin.getConfiguration().isOutOfCombatDeathDroppingExperience()) {
            return;
        }
        // If player is in combat, leave it as is
        if (plugin.getUserManager().getUser(event.getPlayer()).isInCombat()) {
            return;
        }
        // Otherwise, keep experience and drop nothing
        event.setKeepLevel(true);
        event.setShouldDropExperience(false);
        event.setDroppedExp(0);
    }
}
