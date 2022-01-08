package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.profile.PaperProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class ProfileUpdater implements Listener {

    private final @NotNull PaperPlugin plugin;

    public ProfileUpdater(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            PaperProfile profile = plugin.getDatabase().getProfile(player.getUniqueId()).orElseThrow();
            profile.setQuitLocation(player.getLocation());
            plugin.getDatabase().saveProfile(profile);
        } catch (Exception ignored) {
        }
    }

}
