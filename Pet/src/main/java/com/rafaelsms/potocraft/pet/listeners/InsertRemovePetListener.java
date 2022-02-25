package com.rafaelsms.potocraft.pet.listeners;

import com.rafaelsms.potocraft.pet.Permissions;
import com.rafaelsms.potocraft.pet.PetPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.npc.ai.StraightLineNavigationStrategy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InsertRemovePetListener implements Listener {

    private final @NotNull PetPlugin plugin;

    private final Map<UUID, NPC> playersPets = new HashMap<>();
    private @Nullable BukkitTask distanceCheckerTask = null;
    private @Nullable BukkitTask targetSetterTask = null;

    public InsertRemovePetListener(@NotNull PetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void registerTasks(ServerLoadEvent event) {
        if (distanceCheckerTask != null) {
            distanceCheckerTask.cancel();
            distanceCheckerTask = null;
        }
        distanceCheckerTask = plugin.getServer()
                                    .getScheduler()
                                    .runTaskTimer(plugin,
                                                  new DistanceCheckerTask(),
                                                  1,
                                                  plugin.getConfiguration().getDistanceCheckerTimer());
        if (targetSetterTask != null) {
            targetSetterTask.cancel();
            targetSetterTask = null;
        }
        targetSetterTask = plugin.getServer()
                                 .getScheduler()
                                 .runTaskTimer(plugin,
                                               new TargetSetterTask(),
                                               1,
                                               plugin.getConfiguration().getPetTargetSetterTimer());
    }

    @EventHandler(ignoreCancelled = true)
    private void addOnJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(Permissions.PET_PERMISSION)) {
            return;
        }

        NPC npc = plugin.getNpcRegistry()
                        .createNPC(EntityType.FOX,
                                   TextUtil.toColorizedString(Component.text(event.getPlayer().getName())
                                                                       .color(NamedTextColor.GREEN)),
                                   event.getPlayer().getLocation());
        npc.getOrAddTrait(Owner.class).setOwner(event.getPlayer());
        npc.setProtected(true);
        npc.setFlyable(false);
        plugin.getServer().getScheduler().runTaskTimer(plugin, bukkitTask -> {
            if (!event.getPlayer().isOnline()) {
                bukkitTask.cancel();
                return;
            }
            if (!npc.isSpawned()) {
                return;
            }
            if (npc.getEntity() instanceof Ageable ageable) {
                ageable.setBaby();
            }
            npc.getEntity().setSilent(true);
            makeTarget(npc, event.getPlayer());
            bukkitTask.cancel();
        }, 1, 1);
        playersPets.put(event.getPlayer().getUniqueId(), npc);
    }

    @EventHandler(ignoreCancelled = true)
    private void removeOnQuit(PlayerQuitEvent event) {
        NPC npc = playersPets.remove(event.getPlayer().getUniqueId());
        if (npc == null) {
            return;
        }
        npc.destroy();
    }

    private void makeTarget(@NotNull NPC npc, @NotNull Player player) {
        float speedMultiplier = plugin.getConfiguration().getPetSpeedMultiplier();
        npc.getNavigator()
           .setTarget(player,
                      false,
                      parameters -> new StraightLineNavigationStrategy(npc,
                                                                       player,
                                                                       parameters.speedModifier(speedMultiplier)));
    }

    private Optional<Player> getPetOwner(@Nullable NPC npc) {
        if (npc == null || !npc.isSpawned()) {
            return Optional.empty();
        }
        Owner owner = npc.getTraitNullable(Owner.class);
        if (owner == null) {
            return Optional.empty();
        }
        Player player = plugin.getServer().getPlayer(owner.getOwnerId());
        if (player != null && player.isOnline()) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    private class DistanceCheckerTask implements Runnable {

        @Override
        public void run() {
            double maxDistanceFromOwner = plugin.getConfiguration().getMaxDistanceFromOwner();
            double maxDistSq = maxDistanceFromOwner * maxDistanceFromOwner;
            // Check every pet
            for (NPC npc : playersPets.values()) {
                Optional<Player> playerOptional = getPetOwner(npc);
                if (playerOptional.isEmpty()) {
                    continue;
                }
                Player player = playerOptional.get();
                // If pet is too far away, teleport it closer
                if (npc.getEntity().getLocation().distanceSquared(player.getLocation()) >= maxDistSq) {
                    npc.teleport(player.getLocation().add(0, 0.7, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    makeTarget(npc, player);
                }
            }
        }
    }

    private class TargetSetterTask implements Runnable {

        @Override
        public void run() {
            for (NPC npc : playersPets.values()) {
                Optional<Player> playerOptional = getPetOwner(npc);
                if (playerOptional.isEmpty()) {
                    continue;
                }
                // Target player
                makeTarget(npc, playerOptional.get());
            }
        }
    }
}
