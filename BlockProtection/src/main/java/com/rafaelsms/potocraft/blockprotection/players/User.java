package com.rafaelsms.potocraft.blockprotection.players;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.Permissions;
import com.rafaelsms.potocraft.blockprotection.util.Box;
import com.rafaelsms.potocraft.blockprotection.util.LocationBox;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class User {

    private final @NotNull BlockProtectionPlugin plugin;
    private final @NotNull Player player;
    private final @NotNull Profile profile;

    private final @NotNull BlockChangeCache blockChangeCache;
    private @Nullable LocationBox selection = null;

    public User(@NotNull BlockProtectionPlugin plugin, @NotNull Player player, @NotNull Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.blockChangeCache = new BlockChangeCache(this);
        this.profile = profile;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull UUID getPlayerId() {
        return player.getUniqueId();
    }

    public @NotNull Profile getProfile() {
        return profile;
    }

    public boolean hasVolume(int volume) {
        if (player.hasPermission(Permissions.BYPASS_VOLUME_CHECKER)) {
            return true;
        }
        return profile.getVolumeAvailable() >= volume;
    }

    public double getVolumePerBlock() {
        double volume = plugin.getConfiguration().getSelectionVolumeDefaultReward();
        // Check if player has any special permission
        for (Map.Entry<String, Double> entry : plugin.getConfiguration().getSelectionVolumeGroupReward().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                volume = Math.max(entry.getValue(), volume);
            }
        }
        return volume;
    }

    private int getMaxVolumeAllowed() {
        int volume = plugin.getConfiguration().getSelectionVolumeDefaultMaximum();
        // Check if player has any special permission
        for (Map.Entry<String, Integer> entry : plugin.getConfiguration().getSelectionVolumeGroupMaximum().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                volume = Math.max(entry.getValue(), volume);
            }
        }
        return volume;
    }

    public void incrementVolume() {
        double volumeAvailable = this.profile.getVolumeAvailable();
        double volumePerBlock = getVolumePerBlock();
        int maxVolumeAllowed = getMaxVolumeAllowed();
        // If exceeded volume, return
        if (volumeAvailable >= maxVolumeAllowed) {
            return;
        }
        // Else, increment volume per block or remaining value to get to maximum volume allowed
        this.profile.incrementVolume(Math.min(volumePerBlock, Math.max(0.0, maxVolumeAllowed - volumeAvailable)));
    }

    public void clearSelection() {
        this.selection = null;
        this.blockChangeCache.clearBlocks();
    }

    public Optional<Box> getSelection() {
        return Optional.ofNullable(selection);
    }
}
