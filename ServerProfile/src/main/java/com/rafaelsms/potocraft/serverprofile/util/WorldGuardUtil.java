package com.rafaelsms.potocraft.serverprofile.util;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class WorldGuardUtil {

    private WorldGuardUtil() {
    }

    public static @NotNull Optional<LocalPlayer> adapt(@Nullable Player player) {
        return Optional.ofNullable(player).map(p -> WorldGuardPlugin.inst().wrapPlayer(p));
    }

    public static @NotNull Optional<StateFlag> registerFlag(@NotNull ServerProfilePlugin plugin,
                                                            @NotNull String flagName,
                                                            boolean defaultValue,
                                                            @NotNull RegionGroup defaultRegionGroup) {
        Optional<WorldGuard> worldGuard = plugin.getWorldGuard();
        if (worldGuard.isEmpty()) {
            plugin.logger().warn("WorldGuard not present, skipping {} custom flag registry.", flagName);
            return Optional.empty();
        }
        FlagRegistry flagRegistry = worldGuard.get().getFlagRegistry();
        try {
            // Default flag value is false, so every player will have its item damaged by default
            StateFlag flag = new StateFlag(flagName, defaultValue, defaultRegionGroup);
            flagRegistry.register(flag);
            return Optional.of(flag);
        } catch (FlagConflictException exception) {
            plugin.logger().warn("Failed to register {} flag, not doing anything:", flagName, exception);
            return Optional.empty();
        }
    }

    public static @NotNull Optional<ApplicableRegionSet> getApplicableRegions(@NotNull ServerProfilePlugin plugin,
                                                                              @NotNull Location location) {
        Optional<WorldGuard> worldGuard = plugin.getWorldGuard();
        if (worldGuard.isEmpty()) {
            return Optional.empty();
        }
        World world = BukkitAdapter.adapt(location.getWorld());
        RegionManager regionManager = worldGuard.get().getPlatform().getRegionContainer().get(world);
        if (regionManager == null) {
            return Optional.empty();
        }

        BlockVector3 blockVector3 = BukkitAdapter.adapt(location).toVector().toBlockPoint();
        return Optional.of(regionManager.getApplicableRegions(blockVector3));
    }

    public static @NotNull Optional<Boolean> testRegionFlag(@NotNull ServerProfilePlugin plugin,
                                                            @NotNull Location location,
                                                            @Nullable Player player,
                                                            @NotNull StateFlag... stateFlags) {
        return getApplicableRegions(plugin, location).map(set -> set.testState(adapt(player).orElse(null), stateFlags));
    }
}
