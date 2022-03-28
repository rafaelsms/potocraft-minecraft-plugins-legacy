package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.Permissions;
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

public class AreaListener implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    public AreaListener(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void incrementArea(BlockBreakEvent event) {
        if (event.getBlock().getType().isSolid()) {
            plugin.getUserManager().getUser(event.getPlayer()).incrementArea();
        }
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
        if (!event.getPlayer().hasPermission(Permissions.PROTECT_COMMAND)) {
            return;
        }
        // Don't allow interaction with the selection wand
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = plugin.getUserManager().getUser(player);

        // Check if player has minimum volume for selection
        Optional<Selection> selectionOptional = user.getSelection();
        if (selectionOptional.isEmpty() && !user.hasEnoughArea(plugin.getConfiguration().getDefaultBoxArea())) {
            player.sendMessage(plugin.getConfiguration().getSelectionMinimumAreaRequired());
            return;
        }

        // Get or make empty selection if it doesn't exist
        Selection selection = user.getOrEmptySelection();
        Selection.Result result = selection.makeSelection(clickedBlock.getLocation());
        Component playerMessage = switch (result) {
            case WORLD_IS_NOT_PROTECTED -> plugin.getConfiguration().getSelectionWorldNotProtected();
            case SELECTION_MAX_VOLUME_EXCEEDED -> plugin.getConfiguration().getSelectionMaximumAreaExceeded();
            case NOT_ENOUGH_VOLUME_ON_PROFILE -> plugin.getConfiguration().getSelectionNotEnoughArea();
            case INVALID_LOCATION_WORLD -> plugin.getConfiguration().getSelectionInvalidLocation();
            case OTHER_REGION_WITH_PERMISSION_FOUND -> plugin.getConfiguration().getSelectionInsideOtherRegion();
            case OTHER_REGION_WITHOUT_PERMISSION_FOUND -> plugin.getConfiguration().getNoRegionPermission();
            case FAILED_PROTECTION_DATA_FETCH -> plugin.getConfiguration().getFailedToFetchRegions();
            default -> null;
        };

        // Important message, send through chat
        if (result.shouldCancelSelection()) {
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
        // Send tips to the player
        if (result.selectionSucceeded() && selectionOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getSelectionStarted());
        }
    }
}
