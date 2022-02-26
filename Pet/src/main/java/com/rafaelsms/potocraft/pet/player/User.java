package com.rafaelsms.potocraft.pet.player;

import com.rafaelsms.potocraft.pet.PetPlugin;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.PathStrategy;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.npc.ai.StraightLineNavigationStrategy;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class User {

    private final @NotNull PetPlugin plugin;

    private final @NotNull Player player;
    private final @NotNull Profile profile;

    private @Nullable NPC pet = null;
    private int lastDistanceCheck = 0;
    private int lastTargetSet = 0;

    public User(@NotNull PetPlugin plugin, @NotNull Player player, @NotNull Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Profile getProfile() {
        return profile;
    }

    public @NotNull Optional<NPC> getPet() {
        if (!isAvailable()) {
            return Optional.empty();
        }
        assert pet != null;
        return Optional.of(pet);
    }

    public boolean isAvailable() {
        return pet != null && pet.isSpawned() && player.isOnline();
    }

    public void tickPet() {
        // Check if pet is available
        if (!isAvailable()) {
            return;
        }
        assert pet != null;
        // Make entity don't despawn
        pet.getEntity().setTicksLived(1);
        if (pet instanceof LivingEntity livingEntity) {
            livingEntity.setRemoveWhenFarAway(false);
        }
        // Make pet silent
        pet.getEntity().setSilent(true);

        // Teleport if far away
        lastDistanceCheck++;
        if (lastDistanceCheck >= plugin.getConfiguration().getDistanceCheckerTimer()) {
            double petDistSq = player.getLocation().distanceSquared(pet.getEntity().getLocation());
            double maxDist = plugin.getConfiguration().getMaxDistanceFromOwner();
            double maxDistSq = maxDist * maxDist;
            if (petDistSq >= maxDistSq) {
                pet.getEntity().teleportAsync(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                setPetTarget(player);
            }
            lastDistanceCheck = 0;
        }

        // Target player
        lastTargetSet++;
        if (lastTargetSet >= plugin.getConfiguration().getPetTargetSetterTimer()) {
            double petDistSq = pet.getEntity().getLocation().distanceSquared(player.getLocation());
            double maxDist = plugin.getConfiguration().getMinDistanceToTarget();
            double maxDistSq = maxDist * maxDist;
            if (petDistSq >= maxDistSq) {
                setPetTarget();
            }
            lastTargetSet = 0;
        }
    }

    public void spawnPet() {
        despawnPet();
        // Fix pet colored name if not defined, keep if defined
        Optional<String> nameOptional = profile.getPetColoredName();
        String petName;
        if (nameOptional.isEmpty() || nameOptional.get().length() > 16) {
            String randomName = plugin.getConfiguration().getRandomDefaultPetNameList();
            profile.setPetColoredName(randomName);
            plugin.getDatabase().saveProfile(profile);
            petName = randomName;
        } else {
            petName = nameOptional.get();
        }

        pet = plugin.getNpcRegistry()
                    .createNPC(profile.getPetType().orElse(plugin.getConfiguration().getDefaultPetType()),
                               petName,
                               player.getLocation().add(0, 0.7, 0));
        pet.getOrAddTrait(Owner.class).setOwner(player);
        pet.setProtected(true);
        // Set entity
        plugin.getServer().getScheduler().runTaskTimer(plugin, bukkitTask -> {
            if (!player.isOnline() || pet == null) {
                bukkitTask.cancel();
                return;
            }
            if (!pet.isSpawned()) {
                return;
            }
            if (pet.getEntity() instanceof Ageable ageable) {
                if (profile.isPetBaby()) {
                    ageable.setBaby();
                } else {
                    ageable.setAdult();
                }
            }
            attemptSetTarget(player);
            bukkitTask.cancel();
        }, 0, 1);
    }

    public void despawnPet() {
        if (this.pet == null) {
            return;
        }
        this.pet.destroy();
        this.pet = null;
    }

    public void setPetTarget() {
        setPetTarget(player);
    }

    public void setPetTarget(@NotNull Player player) {
        if (pet == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskTimer(plugin, bukkitTask -> {
            if (!player.isOnline() || pet == null) {
                bukkitTask.cancel();
                return;
            }
            attemptSetTarget(player);
            bukkitTask.cancel();
        }, 0, 1);
    }

    private void attemptSetTarget(@NotNull Player player) {
        if (!isAvailable()) {
            return;
        }
        assert pet != null;
        pet.getNavigator().setTarget(player, false, new StrategyFunction(player));
    }

    private class StrategyFunction implements Function<NavigatorParameters, PathStrategy> {

        private final @NotNull Player player;

        private StrategyFunction(@NotNull Player player) {
            this.player = player;
        }

        @Override
        public PathStrategy apply(NavigatorParameters parameters) {
            parameters.speedModifier(plugin.getConfiguration().getPetSpeedMultiplier());
            return new StraightLineNavigationStrategy(pet, player, parameters);
        }
    }
}
