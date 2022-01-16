package com.rafaelsms.potocraft.serverprofile.players;

import org.jetbrains.annotations.NotNull;

public interface TeleportDestination {

    @NotNull org.bukkit.Location getLocation();

    boolean isUnavailable();

    class Player implements TeleportDestination {

        private final @NotNull org.bukkit.entity.Player player;

        public Player(@NotNull org.bukkit.entity.Player player) {
            this.player = player;
        }

        @Override
        public @NotNull org.bukkit.Location getLocation() {
            return player.getLocation();
        }

        @Override
        public boolean isUnavailable() {
            return player.isDead() || !player.isOnline();
        }
    }

    class Location implements TeleportDestination {

        private final @NotNull org.bukkit.Location location;

        public Location(@NotNull org.bukkit.Location location) {
            this.location = location.clone();
        }

        @Override
        public @NotNull org.bukkit.Location getLocation() {
            return location;
        }

        @Override
        public boolean isUnavailable() {
            return location.isWorldLoaded();
        }
    }

}
