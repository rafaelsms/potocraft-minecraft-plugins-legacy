package com.rafaelsms.potocraft.serverprofile.util;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class WorldGuardUtil {

    private WorldGuardUtil() {
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
}
