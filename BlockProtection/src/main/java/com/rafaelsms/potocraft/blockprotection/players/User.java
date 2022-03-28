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

    public void incrementArea() {
        incrementArea(getRewardArea());
    }

    public void incrementArea(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
        int maximumArea = getMaximumArea();
        int areaAvailable = profile.getAreaAvailable();
        // Don't allow bypassing the limit
        if (areaAvailable >= maximumArea) {
            return;
        }
        // Limit how much is incremented
        this.profile.incrementArea(Math.min(maximumArea - areaAvailable, amount));
    }

    public void consumeArea(int area) {
        this.profile.consumeArea(area);
    }

    public int getMaximumArea() {
        int maxArea = plugin.getConfiguration().getSelectionAreaDefaultMaximum();
        for (Map.Entry<String, Integer> entry : plugin.getConfiguration().getSelectionAreaGroupMaximum().entrySet()) {
            if (entry.getValue() > maxArea && player.hasPermission(entry.getKey())) {
                maxArea = entry.getValue();
            }
        }
        return maxArea;
    }

    public double getRewardArea() {
        double reward = plugin.getConfiguration().getSelectionAreaDefaultReward();
        for (Map.Entry<String, Double> entry : plugin.getConfiguration().getSelectionAreaGroupReward().entrySet()) {
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

    public boolean hasEnoughArea(int area) {
        if (player.hasPermission(Permissions.BYPASS_AREA_CHECKER)) {
            return true;
        }
        return profile.getAreaAvailable() >= area;
    }

    @Override
    public void run() {
        if (selection != null) {
            selection.run();
        }
    }
}
