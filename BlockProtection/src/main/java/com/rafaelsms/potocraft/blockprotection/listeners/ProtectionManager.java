package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.blockprotection.util.Selection;
import com.rafaelsms.potocraft.blockprotection.util.SelectionBox;
import com.rafaelsms.potocraft.database.Database;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionManager implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    // Player -> Selection box
    // This is not on User instance because we need to check against each other and with existing protections
    private final Map<UUID, Selection> selectionBoxes = Collections.synchronizedMap(new HashMap<>());

    public ProtectionManager(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized boolean addPlayerSelection(@NotNull Player player) {
        User user = plugin.getUserManager().getUser(player);
        return this.selectionBoxes.put(user.getPlayerId(), new Selection(plugin, user)) == null;
    }

    public synchronized boolean removePlayerSelection(@NotNull UUID playerId) {
        Selection selection = this.selectionBoxes.remove(playerId);
        if (selection != null) {
            selection.hideBar();
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private synchronized void addSelection(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        Selection existingSelection = selectionBoxes.get(event.getPlayer().getUniqueId());
        if (existingSelection == null) {
            return;
        }

        // Make selection
        Location clickedLocation = clickedBlock.getLocation();
        SelectionBox newSelectionBox =
                SelectionBox.makeSelection(plugin, existingSelection.getBox().orElse(null), clickedLocation);

        // Check if the player has volume to do this operation
        User user = plugin.getUserManager().getUser(event.getPlayer());
        if (!user.hasVolume(newSelectionBox.getVolume())) {
            event.getPlayer().sendMessage("don't have enough volume for selection");
            return;
        }

        // Check if expanded selection collides with any other
        for (Selection otherSelection : selectionBoxes.values()) {
            if (otherSelection.getBox().isEmpty()) {
                continue;
            }
            if (newSelectionBox.intersects(otherSelection.getBox().get())) {
                event.getPlayer().sendMessage("intersected with another selection, didn't update.");
                return;
            }
        }
        // Check if collides with already existing regions on the database
        try {
            if (plugin.getDatabase().collidesWith(newSelectionBox)) {
                event.getPlayer().sendMessage("intersected with existing region, didn't update.");
                return;
            }
        } catch (Database.DatabaseException ignored) {
            // On error, don't expand
            event.getPlayer().sendMessage("Failed to check database for collisions, didn't update.");
            return;
        }

        // Update existing selection
        existingSelection.setBox(newSelectionBox);
    }

    @EventHandler
    private synchronized void clearSelection(PlayerQuitEvent event) {
        removePlayerSelection(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private synchronized void clearSelection(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (removePlayerSelection(player.getUniqueId())) {
            player.sendMessage("Selection cancelled!");
        }
    }
}
