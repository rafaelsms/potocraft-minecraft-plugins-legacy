package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.util.WorldGuardUtil;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorldGuardRequireInitialMemberDamageFlag implements Listener {

    private final Map<UUID, Map<UUID, ZonedDateTime>> allowedToAttack = new HashMap<>();

    private final @NotNull ServerProfilePlugin plugin;

    public WorldGuardRequireInitialMemberDamageFlag(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void removeAllowedToAttackOnDeath(PlayerDeathEvent event) {
        allowedToAttack.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    private void removeAllowedToAttackOnQuit(PlayerQuitEvent event) {
        allowedToAttack.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerVersusPlayerDamage(EntityDamageByEntityEvent event) {
        // Ignore non-players
        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }

        // Check if damager is a player
        Player damager;
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        } else if (event.getDamager() instanceof Player playerDamager) {
            damager = playerDamager;
        } else {
            return;
        }

        // Get applicable regions
        Optional<ApplicableRegionSet> applicableRegionsOptional =
                WorldGuardUtil.getApplicableRegions(plugin, damaged.getLocation());
        if (applicableRegionsOptional.isEmpty()) {
            // Prevent damage anyway, it is safer
            plugin.logger().error("No region manager found! Preventing PVP damage as a precaution.");
            event.setCancelled(true);
            return;
        }

        // Ignore if we do not require initial member damage
        ApplicableRegionSet applicableRegions = applicableRegionsOptional.get();
        if (!applicableRegions.testState(null, ServerProfilePlugin.REQUIRE_INITIAL_MEMBER_DAMAGE)) {
            return;
        }

        // If player DAMAGED is NOT a member, allow damage to happen
        if (!isMember(applicableRegions, WorldGuardUtil.adapt(damaged).orElseThrow())) {
            // If player DAMAGER IS a member, allow DAMAGED player to attack back
            if (isMember(applicableRegions, WorldGuardUtil.adapt(damager).orElseThrow())) {
                Map<UUID, ZonedDateTime> allowed = allowedToAttack.getOrDefault(damager.getUniqueId(), new HashMap<>());
                allowed.put(damaged.getUniqueId(), ZonedDateTime.now());
                allowedToAttack.put(damager.getUniqueId(), allowed);
            }
            return;
        }

        // If DAMAGED is a member, check if DAMAGER was allowed
        ZonedDateTime zonedDateTime =
                allowedToAttack.getOrDefault(damaged.getUniqueId(), Map.of()).get(damager.getUniqueId());
        if (zonedDateTime != null) {
            // If the current date time is after the minimum date time to allow, we allow it
            ZonedDateTime minDateTime =
                    ZonedDateTime.now().minus(plugin.getConfiguration().getPlayerSafeRegionCombatTimeout());
            if (zonedDateTime.isAfter(minDateTime)) {
                return;
            }
        }

        // Since DAMAGED is a member and DAMAGER was not allowed, cancel damage
        event.setCancelled(true);
    }

    private boolean isMember(@NotNull ApplicableRegionSet applicableRegions, @NotNull LocalPlayer player) {
        for (ProtectedRegion region : applicableRegions.getRegions()) {
            if (region.isMember(player)) {
                return true;
            }
        }
        return false;
    }
}
