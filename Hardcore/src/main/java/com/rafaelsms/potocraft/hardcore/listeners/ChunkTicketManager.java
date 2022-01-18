package com.rafaelsms.potocraft.hardcore.listeners;

import com.rafaelsms.potocraft.hardcore.HardcorePlugin;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChunkTicketManager implements Listener {

    private final HashMap<Chunk, BukkitTask> ticketRemoveTask = new HashMap<>();

    private final @NotNull HardcorePlugin plugin;

    public ChunkTicketManager(@NotNull HardcorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void addTicketOnDeath(PlayerDeathEvent event) {
        Chunk chunk = event.getPlayer().getChunk();
        // Add a plugin ticket
        chunk.addPluginChunkTicket(plugin);

        // Create a task to remove the ticket
        ChunkTicketRemoveRunnable runnable = new ChunkTicketRemoveRunnable(chunk);
        BukkitTask newTask = plugin
                .getServer()
                .getScheduler()
                .runTaskLater(plugin, runnable, plugin.getConfiguration().getChunkActiveTicksAfterDeath());
        BukkitTask oldTask = ticketRemoveTask.put(chunk, newTask);
        // If an older task exists, cancel it
        if (oldTask != null) {
            oldTask.cancel();
        }
    }

    private class ChunkTicketRemoveRunnable implements Runnable {

        private final @NotNull Chunk chunk;

        private ChunkTicketRemoveRunnable(@NotNull Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run() {
            // Remove the ticket and the task from the map
            this.chunk.removePluginChunkTicket(plugin);
            ticketRemoveTask.remove(chunk);
        }
    }
}
