package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class DamageEffects implements Listener {

    private final List<Color> fireworkColors = List.of(Color.RED,
                                                       Color.ORANGE,
                                                       Color.LIME,
                                                       Color.AQUA,
                                                       Color.GREEN,
                                                       Color.WHITE,
                                                       Color.YELLOW,
                                                       Color.SILVER);
    private final List<FireworkEffect.Type> fireworkTypes = List.of(FireworkEffect.Type.BALL,
                                                                    FireworkEffect.Type.BALL_LARGE,
                                                                    FireworkEffect.Type.CREEPER,
                                                                    FireworkEffect.Type.STAR,
                                                                    FireworkEffect.Type.BURST);

    private final @NotNull ServerUtilityPlugin plugin;

    public DamageEffects(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void damageEffect(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity &&
            livingEntity.getNoDamageTicks() <= (livingEntity.getMaximumNoDamageTicks() / 2)) {
            showDamageEffect(livingEntity);
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
            !killer.isDead() &&
            !Objects.equals(killer.getUniqueId(), event.getPlayer().getUniqueId())) {
            Firework firework = killer.getWorld()
                                      .spawn(killer.getEyeLocation(),
                                             Firework.class,
                                             CreatureSpawnEvent.SpawnReason.COMMAND);
            firework.setShotAtAngle(false);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(1); // 0.5 seconds on air (2.5 is too much)
            meta.addEffect(FireworkEffect.builder()
                                         .flicker(true)
                                         .trail(true)
                                         .withColor(Util.getRandom(fireworkColors))
                                         .withFade(Util.getRandom(fireworkColors), Util.getRandom(fireworkColors))
                                         .with(Util.getRandom(fireworkTypes))
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
                                 entity.getWidth() * 0.38,
                                 entity.getHeight() * 0.64,
                                 entity.getWidth() * 0.38,
                                 blockData);
        }
    }
}
