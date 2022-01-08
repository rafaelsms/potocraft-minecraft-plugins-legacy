package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfileUpdater implements Listener {

    private final PaperPlugin plugin;

    public ProfileUpdater(PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabase().getProfile(player).whenComplete((profile, retrievalThrowable) -> {
            // If there was an issue with profile retrieval, warn and return
            if (retrievalThrowable != null) {
                plugin.logger().warn("Failed to retrieve profile for player %s (uuid = %s) on quit: %s"
                        .formatted(player.getName(), player.getUniqueId(), retrievalThrowable.getLocalizedMessage()));
                return;
            }
            if (profile == null) {
                return;
            }
            if (plugin.getSettings().getServerName().equalsIgnoreCase(plugin.getSettings().getLoginServer())) {
                return;
            }

            profile.setQuitLocation();
            plugin.getDatabase().saveProfile(profile).whenComplete((unused, saveThrowable) -> {
                if (saveThrowable != null) {
                    plugin.logger().warn("Failed to save profile for player %s (uuid = %s) on quit: %s"
                            .formatted(player.getName(), player.getUniqueId(), saveThrowable.getLocalizedMessage()));
                }
            });
        });
    }

}
