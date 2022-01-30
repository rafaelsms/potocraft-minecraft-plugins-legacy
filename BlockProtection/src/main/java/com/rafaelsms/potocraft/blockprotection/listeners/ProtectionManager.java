package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.util.SelectionBox;
import com.rafaelsms.potocraft.database.Database;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class ProtectionManager implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    // Player -> Selection box
    // This is not on User instance because we need to check against each other and with existing protections
    private final HashMap<UUID, SelectionBox> selectionBoxes = new HashMap<>();

    public ProtectionManager(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void clearSelection(PlayerQuitEvent event) {
        this.selectionBoxes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void addSelection(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!selectionBoxes.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        // Make selection, check if it collides with any other
        Location clickedLocation = clickedBlock.getLocation();
        SelectionBox existingSelection = selectionBoxes.get(event.getPlayer().getUniqueId());
        SelectionBox selection = SelectionBox.makeSelection(plugin, existingSelection, clickedLocation);
        for (SelectionBox otherBox : selectionBoxes.values()) {
            if (selection.intersects(otherBox)) {
                event.getPlayer().sendMessage("intersected with another selection, didn't update.");
                return;
            }
        }
        // Check any collision on the database
        try {
            if (plugin.getDatabase().collidesWith(selection)) {
                event.getPlayer().sendMessage("intersected with existing region, didn't update.");
                return;
            }
        }catch (Database.DatabaseException ignored) {
            event.getPlayer().sendMessage("Failed to check database for collisions, didn't update.");
            return;
        }

        // Update existing selection
        this.selectionBoxes.put(event.getPlayer().getUniqueId(), selection);
    }
}
