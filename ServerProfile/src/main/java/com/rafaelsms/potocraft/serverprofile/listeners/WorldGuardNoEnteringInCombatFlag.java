package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WorldGuardNoEnteringInCombatFlag extends Handler {

    private static ServerProfilePlugin plugin;

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<WorldGuardNoEnteringInCombatFlag> {
        @Override
        public WorldGuardNoEnteringInCombatFlag create(Session session) {
            return new WorldGuardNoEnteringInCombatFlag(session);
        }
    }

    public WorldGuardNoEnteringInCombatFlag(@NotNull Session session) {
        super(session);
    }

    public static void setPlugin(@NotNull ServerProfilePlugin plugin) {
        WorldGuardNoEnteringInCombatFlag.plugin = plugin;
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player,
                                   Location from,
                                   Location to,
                                   ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered,
                                   Set<ProtectedRegion> exited,
                                   MoveType moveType) {
        // Ignore if on the same regions (we just prevent entry)
        if (entered.isEmpty()) {
            return true;
        }
        // Ignore if it allows entering region while in combat
        if (toSet.testState(player, ServerProfilePlugin.ENTERING_IN_COMBAT_FLAG)) {
            return true;
        }

        Player bukkitPlayer = BukkitAdapter.adapt(player);
        User user = plugin.getUserManager().getUser(bukkitPlayer);
        // Ignore if player is not in combat
        if (!user.isInCombat(plugin.getConfiguration().getPreventEnteringCombatTypeRequired())) {
            return true;
        }

        // Prevent entry if region has no PVP
        if (!toSet.testState(player, Flags.PVP)) {
            return false;
        }
        // Prevent entry if player is member of any entered region
        for (ProtectedRegion protectedRegion : entered) {
            if (protectedRegion.isMember(player)) {
                return false;
            }
        }
        // Otherwise, allow (PVP is enabled and player is not member, thus not being protected in this region)
        return true;
    }
}
