package com.rafaelsms.potocraft.blockprotection.players;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.Permissions;
import com.rafaelsms.potocraft.blockprotection.protection.Selection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class User implements Runnable {

    private final @NotNull BlockProtectionPlugin plugin;

    private final @NotNull Player player;
    private final @NotNull Profile profile;

    private @Nullable Selection selection;

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

    public boolean hasEnoughVolume(int volume) {
        if (player.hasPermission(Permissions.BYPASS_VOLUME_CHECKER)) {
            return true;
        }
        return profile.getVolumeAvailable() >= volume;
    }

    public void setSelection(@Nullable Selection selection) {
        if (this.selection == selection) {
            return;
        }
        if (this.selection != null) {
            this.selection.hideBar();
        }
        if (selection != null) {
            selection.showBar();
        }
        this.selection = selection;
    }

    @Override
    public void run() {
        if (this.selection != null) {
            this.selection.run();
        }
    }
}
