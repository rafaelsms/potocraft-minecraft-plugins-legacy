package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.blockprotection.protection.Selection;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VolumeListener implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    public VolumeListener(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void incrementVolume(BlockBreakEvent event) {
        plugin.getUserManager().getUser(event.getPlayer()).incrementVolume();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void makeSelection(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (event.getItem() == null ||
            event.getItem().getType() != plugin.getConfiguration().getSelectionWandMaterial()) {
            return;
        }
        // Don't allow interaction with the selection wand
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = plugin.getUserManager().getUser(player);

        // Check if player has minimum volume for selection
        Optional<Selection> selectionOptional = user.getSelection();
        if (selectionOptional.isEmpty() && !user.hasEnoughVolume(plugin.getConfiguration().getDefaultBoxVolume())) {
            player.sendMessage(plugin.getConfiguration().getSelectionMinimumVolumeRequired());
            return;
        }

        // Get or make empty selection if it doesn't exist
        Selection selection = user.getOrEmptySelection();
        Selection.Result result = selection.select(clickedBlock.getLocation());
        Component playerMessage = switch (result) {
            case WORLD_NOT_PROTECTED -> plugin.getConfiguration().getSelectionWorldNotProtected();
            case MAX_VOLUME_EXCEEDED -> plugin.getConfiguration().getSelectionMaximumVolumeExceeded();
            case NOT_ENOUGH_VOLUME -> plugin.getConfiguration().getSelectionVolumeExceeded();
            case VOLUME_EXCEED_PERMISSION -> plugin.getConfiguration().getSelectionVolumeExceedPermission();
            case INVALID_LOCATION -> plugin.getConfiguration().getSelectionInvalidLocation();
            case OTHER_REGION_FOUND -> plugin.getConfiguration().getSelectionInsideOtherRegion();
            case NO_PERMISSION -> plugin.getConfiguration().getNoRegionPermission();
            case FAILED_PROTECTION_FETCH -> plugin.getConfiguration().getFailedToFetchRegions();
            default -> null;
        };

        // Important message, send through chat
        if (result.shouldCancel()) {
            if (playerMessage != null) {
                player.sendMessage(playerMessage);
            }
            user.setSelection(null);
            return;
        }
        // Simple warning, show on action bar
        if (playerMessage != null) {
            player.sendActionBar(playerMessage);
        }
    }
}
