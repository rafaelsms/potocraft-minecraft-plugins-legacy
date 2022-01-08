package com.rafaelsms.potocraft.velocity.user;

import com.rafaelsms.potocraft.user.UserProfile;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

public class VelocityUser extends UserProfile {

    private final @NotNull VelocityPlugin plugin;

    public VelocityUser(@NotNull VelocityPlugin plugin, @NotNull UUID playerId, @NotNull String playerName) {
        super(plugin, playerId, playerName);
        this.plugin = plugin;
    }

    public VelocityUser(@NotNull VelocityPlugin plugin, @NotNull Document document) {
        super(plugin, document);
        this.plugin = plugin;
    }

    public boolean isLoggedIn(@Nullable InetSocketAddress address) {
        if (address == null || this.lastLoginAddress == null || this.lastLoginDate == null)
            return false;
        long maxLoginDurationSeconds = plugin.getSettings().getMaxLoginDurationSeconds();
        if (this.lastLoginDate.isAfter(ZonedDateTime.now().minus(Duration.ofSeconds(maxLoginDurationSeconds))))
            return false;
        return Util.getIpAddress(address).equalsIgnoreCase(this.lastLoginAddress);
    }

    public void setLoggedIn(@Nullable InetSocketAddress address) {
        super.setLoggedIn(address);
    }

    public void updateJoinDate() {
        super.updateJoinDate();
    }

    public void updateQuitDate() {
        super.updateQuitDate();
    }
}
