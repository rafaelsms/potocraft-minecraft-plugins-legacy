package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
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
        Player bukkitPlayer = BukkitAdapter.adapt(player);
        for (ProtectedRegion protectedRegion : toSet.getRegions()) {
            if (protectedRegion.getFlag(ServerProfilePlugin.SHOW_MEMBERS_FLAG) == StateFlag.State.ALLOW) {
                bukkitPlayer.sendMessage(plugin.getConfiguration()
                                               .getEnteringRegionMessage(getMembersNames(protectedRegion)));
            }
        }
        for (ProtectedRegion protectedRegion : exited) {
            if (protectedRegion.getFlag(ServerProfilePlugin.SHOW_MEMBERS_FLAG) == StateFlag.State.ALLOW) {
                bukkitPlayer.sendMessage(plugin.getConfiguration()
                                               .getLeavingRegionMessage(getMembersNames(protectedRegion)));
            }
        }
        return true;
    }

    private Set<String> getMembersNames(@NotNull ProtectedRegion protectedRegion) {
        Set<String> playerNames = new LinkedHashSet<>();
        for (UUID playerIds : protectedRegion.getOwners().getUniqueIds()) {
            playerNames.add(Optional.ofNullable(plugin.getServer().getOfflinePlayer(playerIds).getName())
                                    .orElse(plugin.getConfiguration().getUnknownPlayerName()));
        }
        for (UUID playerIds : protectedRegion.getMembers().getUniqueIds()) {
            playerNames.add(Optional.ofNullable(plugin.getServer().getOfflinePlayer(playerIds).getName())
                                    .orElse(plugin.getConfiguration().getUnknownPlayerName()));
        }
        return playerNames;
    }
}
