package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

public class DamageEffects implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public DamageEffects(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void damageEffect(EntityDamageEvent event) {
        if (event.getEntity() instanceof Mob mob && mob.getNoDamageTicks() > (mob.getMaximumNoDamageTicks() / 2)) {
            showDamageEffect(mob);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void lightningTheDead(PlayerDeathEvent event) {
        if (plugin.getConfiguration().isSpawnLightningOnDead()) {
            event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
        }
        Player killer = event.getPlayer().getKiller();
        if (plugin.getConfiguration().isSpawnFireworkOnKiller() &&
            killer != null &&
            killer.isOnline() &&
            !killer.isDead()) {
            Firework firework = killer.getWorld()
                                      .spawn(killer.getEyeLocation(),
                                             Firework.class,
                                             CreatureSpawnEvent.SpawnReason.COMMAND);
            firework.setShotAtAngle(false);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(2 * 2 + 1); // 2.5 seconds on air
            meta.addEffect(FireworkEffect.builder()
                                         .flicker(true)
                                         .trail(true)
                                         .withColor(Color.RED)
                                         .withFade(Color.YELLOW, Color.ORANGE)
                                         .with(FireworkEffect.Type.BALL_LARGE)
                                         .build());
            firework.setFireworkMeta(meta);
        }
    }

    private void showDamageEffect(@NotNull Entity entity) {
        int particleAmount = plugin.getConfiguration().getDamageParticleAmount();
        if (particleAmount > 0) {
            Location effectLocation = entity.getLocation();
            BlockData blockData = switch (entity.getType()) {
                case BAT, SPIDER, WITHER_SKELETON, WITHER -> Material.COAL_BLOCK.createBlockData();
                case SKELETON, SKELETON_HORSE, GHAST, IRON_GOLEM, SNOWMAN -> Material.BONE_BLOCK.createBlockData();
                case CREEPER, CAVE_SPIDER -> Material.SLIME_BLOCK.createBlockData();
                case BLAZE -> Material.MAGMA_BLOCK.createBlockData();
                case ENDERMAN, ENDERMITE, ENDER_DRAGON -> Material.CRYING_OBSIDIAN.createBlockData();
                default -> Material.REDSTONE_BLOCK.createBlockData();
            };
            entity.getWorld()
                  .spawnParticle(Particle.BLOCK_DUST,
                                 effectLocation,
                                 particleAmount,
                                 entity.getWidth() * 4.0 / 3.0,
                                 entity.getHeight() * 4.0 / 3.0,
                                 entity.getWidth() * 4.0 / 3.0,
                                 blockData);
        }
    }
}
