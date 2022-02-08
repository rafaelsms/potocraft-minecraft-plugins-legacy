package com.rafaelsms.potocraft.serverutility.tasks;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.WorldCombatConfig;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class TimedPVPTask implements Runnable {

    private final @NotNull ServerUtilityPlugin plugin;

    public TimedPVPTask(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World world : plugin.getServer().getWorlds()) {
            WorldCombatConfig combatConfig = plugin.getConfiguration().getCombatConfiguration(world.getName());
            if (combatConfig.isConstantCombat()) {
                continue;
            }

            boolean enabledBetweenRange = combatConfig.getEndCombatTime() > combatConfig.getStartCombatTime();
            int lowerTime = Math.min(combatConfig.getStartCombatTime(), combatConfig.getEndCombatTime());
            int higherTime = Math.max(combatConfig.getStartCombatTime(), combatConfig.getEndCombatTime());
            boolean inBetween = lowerTime <= world.getTime() && world.getTime() <= higherTime;
            boolean pvpStatus = (enabledBetweenRange && inBetween) || (!enabledBetweenRange && !inBetween);
            world.setPVP(pvpStatus);
        }
    }
}
