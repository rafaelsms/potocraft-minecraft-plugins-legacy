package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class WorldGuardShowMemberFlag extends Handler {

    private static ServerProfilePlugin plugin;

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<WorldGuardShowMemberFlag> {
        @Override
        public WorldGuardShowMemberFlag create(Session session) {
            return new WorldGuardShowMemberFlag(session);
        }
    }

    public WorldGuardShowMemberFlag(@NotNull Session session) {
        super(session);
    }

    public static void setPlugin(@NotNull ServerProfilePlugin plugin) {
        WorldGuardShowMemberFlag.plugin = plugin;
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player,
                                   Location from,
                                   Location to,
                                   ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered,
                                   Set<ProtectedRegion> exited,
                                   MoveType moveType) {
        // Ignore if no change in which regions we are in
        if (entered.isEmpty() && exited.isEmpty()) {
            return true;
        }
        // Ignore if no show members flag
        if (!toSet.testState(player, ServerProfilePlugin.SHOW_MEMBERS_FLAG)) {
            return true;
        }

        // For each entered region, show its members
        Player bukkitPlayer = BukkitAdapter.adapt(player);
        Set<String> enteredRegionsMembers = new HashSet<>();
        for (ProtectedRegion protectedRegion : entered) {
            accumulateMembersNames(protectedRegion, enteredRegionsMembers);
        }
        if (!enteredRegionsMembers.isEmpty()) {
            bukkitPlayer.sendMessage(plugin.getConfiguration().getEnteringRegionMessage(enteredRegionsMembers));
        }

        // For each exiting region, show its members
        Set<String> exitingRegionsMembers = new HashSet<>();
        for (ProtectedRegion protectedRegion : exited) {
            accumulateMembersNames(protectedRegion, exitingRegionsMembers);
        }
        if (!exitingRegionsMembers.isEmpty()) {
            bukkitPlayer.sendMessage(plugin.getConfiguration().getLeavingRegionMessage(exitingRegionsMembers));
        }
        return true;
    }

    private void accumulateMembersNames(@NotNull ProtectedRegion protectedRegion,
                                        @NotNull Set<String> accumulatingNames) {
        for (UUID playerIds : protectedRegion.getOwners().getUniqueIds()) {
            accumulatingNames.add(Optional.ofNullable(plugin.getServer().getOfflinePlayer(playerIds).getName())
                                          .orElse(plugin.getConfiguration().getUnknownPlayerName()));
        }
        for (UUID playerIds : protectedRegion.getMembers().getUniqueIds()) {
            accumulatingNames.add(Optional.ofNullable(plugin.getServer().getOfflinePlayer(playerIds).getName())
                                          .orElse(plugin.getConfiguration().getUnknownPlayerName()));
        }
    }
}
