package com.rafaelsms.potocraft.blockprotection.players;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private @Nullable PlayerSelection selection = null;

    public User(@NotNull BlockProtectionPlugin plugin, @NotNull Player player, @NotNull Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.blockChangeCache = new BlockChangeCache(plugin, this);
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
        this.profile.incrementVolume(getVolumePerBlock(), getMaxVolumeAllowed());
    }

    /**
     * Selects a block to start protecting. It'll increase current selection if it is in the same world and if there is
     * enough volume available on the profile.
     *
     * @param blockLocation block selected
     * @return true if selection was successfully increased, false if it was volume-limited.
     * @see Profile#getVolumeAvailable() for current volume available
     */
    public boolean addSelection(@NotNull Location blockLocation) {
        boolean allowed = true;
        // Create or increase selection
        if (this.selection == null || !this.selection.getWorldId().equals(blockLocation.getWorld().getUID())) {
            // Create a new one in case world changed or wasn't selecting
            PlayerSelection selection = new PlayerSelection(plugin, blockLocation);
            // Ignore if volume exceed maximum volume
            if (selection.getVolume() > profile.getVolumeAvailable()) {
                this.blockChangeCache.clearBlocks();
                this.selection = null;
                return false;
            }
            this.selection = selection;
        } else {
            allowed = this.selection.limitedSelect(blockLocation, getMaxVolumeAllowed());
        }
        // Show current selection
        this.blockChangeCache.showRectangle(selection.getCorner1(),
                                            selection.getCorner2(),
                                            Material.GLOWSTONE.createBlockData());
        return allowed;
    }

    public void clearSelection() {
        this.selection = null;
        this.blockChangeCache.clearBlocks();
    }

    public Optional<PlayerSelection> getSelection() {
        return Optional.ofNullable(selection);
    }
}
