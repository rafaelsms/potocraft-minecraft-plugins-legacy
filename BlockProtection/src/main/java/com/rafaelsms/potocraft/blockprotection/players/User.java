package com.rafaelsms.potocraft.blockprotection.players;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.Permissions;
import com.rafaelsms.potocraft.blockprotection.protection.Selection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class User implements Runnable {

    private final @NotNull BlockProtectionPlugin plugin;

    private final @NotNull Player player;
    private final @NotNull Profile profile;

    private @Nullable Selection selection = null;

    public User(@NotNull BlockProtectionPlugin plugin, @NotNull Player player, @NotNull Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Profile getProfile() {
        return profile;
    }

    public @NotNull Optional<Selection> getSelection() {
        return Optional.ofNullable(selection);
    }

    public @NotNull Selection getOrEmptySelection() {
        Optional<Selection> selectionOptional = getSelection();
        if (selectionOptional.isEmpty()) {
            Selection selection = new Selection(plugin, this);
            setSelection(selection);
            return selection;
        }
        return selectionOptional.get();
    }

    public void setSelection(@Nullable Selection selection) {
        this.selection = selection;
    }

    public void incrementVolume() {
        incrementVolume(getRewardVolume());
    }

    public void incrementVolume(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Volume must be greater than zero");
        }
        int maximumVolume = getMaximumVolume();
        int volumeAvailable = profile.getVolumeAvailable();
        // Don't allow bypassing the limit
        if (volumeAvailable >= maximumVolume) {
            return;
        }
        // Limit how much is incremented
        this.profile.incrementVolume(Math.min(maximumVolume - volumeAvailable, amount));
    }

    public void consumeVolume(int volume) {
        this.profile.consumeVolume(volume);
    }

    public int getMaximumVolume() {
        int maxVolume = plugin.getConfiguration().getSelectionVolumeDefaultMaximum();
        for (Map.Entry<String, Integer> entry : plugin.getConfiguration().getSelectionVolumeGroupMaximum().entrySet()) {
            if (entry.getValue() > maxVolume && player.hasPermission(entry.getKey())) {
                maxVolume = entry.getValue();
            }
        }
        return maxVolume;
    }

    public double getRewardVolume() {
        double reward = plugin.getConfiguration().getSelectionVolumeDefaultReward();
        for (Map.Entry<String, Double> entry : plugin.getConfiguration().getSelectionVolumeGroupReward().entrySet()) {
            if (entry.getValue() > reward && player.hasPermission(entry.getKey())) {
                reward = entry.getValue();
            }
        }
        return reward;
    }

    public double getDeletionPayback() {
        double paybackRatio = plugin.getConfiguration().getDeletionDefaultPayback();
        for (Map.Entry<String, Double> entry : plugin.getConfiguration().getGroupDeletionPayback().entrySet()) {
            if (entry.getValue() > paybackRatio && player.hasPermission(entry.getKey())) {
                paybackRatio = entry.getValue();
            }
        }
        return paybackRatio;
    }

    public boolean hasEnoughVolume(int volume) {
        if (player.hasPermission(Permissions.BYPASS_VOLUME_CHECKER)) {
            return true;
        }
        return profile.getVolumeAvailable() >= volume;
    }

    @Override
    public void run() {
        if (selection != null) {
            selection.run();
        }
    }
}
