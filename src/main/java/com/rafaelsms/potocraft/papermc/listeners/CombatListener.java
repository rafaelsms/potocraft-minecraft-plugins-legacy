package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.profile.Location;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.papermc.user.combat.CombatTask;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CombatListener implements Listener {

    private final @NotNull PaperPlugin plugin;

    public CombatListener(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void storeDeathLocation(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
        user
                .getServerProfile()
                .setDeathLocation(Location.fromPlayer(plugin.getSettings().getServerName(), player.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    private void showDeathLocation(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
        // Skip if player can't see death location or if it is null
        Optional<Location> optionalLocation = user.getServerProfile().getDeathLocation();
        if (optionalLocation.isEmpty() || !player.hasPermission(Permissions.COMBAT_SHOW_DEATH_LOCATION)) {
            return;
        }
        // Show to the player
        org.bukkit.Location location = optionalLocation.get().toBukkit(plugin.getServer());
        player.sendMessage(plugin.getSettings().getCombatDeathLocation(location));
    }

    @EventHandler(ignoreCancelled = true)
    private void killIfLeftCombat(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
        // Ignore if player is not in combat
        if (user.getCombatTask().isEmpty()) {
            return;
        }
        // Check whether we kill the player or destroy its totem
        if (plugin.getSettings().getCombatShouldUseTotem()) {
            player.damage(Double.MAX_VALUE);
        } else {
            player.setHealth(0.0);
        }
        plugin
                .logger()
                .info("Player %s (UUID = %s) died in combat".formatted(player.getName(),
                                                                       player.getUniqueId().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void setOutOfCombat(PlayerDeathEvent event) {
        PaperUser user = plugin.getUserManager().getUser(event.getPlayer().getUniqueId());
        user.setCombatTask(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void setInCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) {
            return;
        }
        PaperUser damagedUser = plugin.getUserManager().getUser(damagedPlayer.getUniqueId());

        Entity damagerEntity = event.getDamager();
        if (damagerEntity instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooterEntity) {
            damagerEntity = shooterEntity;
        }

        if (damagerEntity instanceof Player damagerPlayer) {
            long delayTicks = plugin.getSettings().getCombatVsPlayersTicks();

            // Set in combat for the damaged player
            if (!damagedPlayer.hasPermission(Permissions.COMBAT_PLAYERS_BYPASS)) {
                damagedUser.setCombatTask(new CombatTask(plugin, damagedUser, delayTicks));
            }

            // Set in combat for the damager user as well
            if (!damagerPlayer.hasPermission(Permissions.COMBAT_PLAYERS_BYPASS)) {
                PaperUser damagerUser = plugin.getUserManager().getUser(damagerPlayer.getUniqueId());
                damagerUser.setCombatTask(new CombatTask(plugin, damagerUser, delayTicks));
            }
        } else {
            // Set in combat for the damaged player
            if (!damagedPlayer.hasPermission(Permissions.COMBAT_MOBS_BYPASS)) {
                long delayTicks = plugin.getSettings().getCombatVsMobsTicks();
                damagedUser.setCombatTask(new CombatTask(plugin, damagedUser, delayTicks));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void cancelCommandInCombat(PlayerCommandPreprocessEvent event) {
        // Get command label
        String[] arguments = TextUtil.parseArguments(event.getMessage());
        if (arguments.length == 0) {
            return;
        }
        String commandLabel = arguments[0];
        // Find command
        PluginCommand command = plugin.getServer().getPluginCommand(commandLabel);
        // Attempt its aliases
        if (command == null) {
            for (Map.Entry<String, String[]> entry : plugin.getServer().getCommandAliases().entrySet()) {
                for (String alias : entry.getValue()) {
                    if (!alias.equalsIgnoreCase(commandLabel)) {
                        continue;
                    }
                    command = plugin.getServer().getPluginCommand(entry.getKey());
                    // Stop search only if we found a command
                    if (command != null) {
                        break;
                    }
                }
            }
        }
        if (command == null) {
            return;
        }

        // If a command was found, check if it is on the list of blocked commands
        List<String> blockedCommands = plugin.getSettings().getCombatBlockedCommands();
        if (command.getName().equalsIgnoreCase(commandLabel)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getSettings().getCombatBlockedCommand());
            return;
        }
        // Find within its aliases
        for (String alias : command.getAliases()) {
            for (String blockedCommand : blockedCommands) {
                if (alias.equalsIgnoreCase(blockedCommand)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(plugin.getSettings().getCombatBlockedCommand());
                    return;
                }
            }
        }
    }
}
