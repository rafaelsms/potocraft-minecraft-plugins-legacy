package com.rafaelsms.potocraft.serverprofile.listeners;

import com.earth2me.essentials.Essentials;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class EssentialsImporter implements Listener {

    private final @NotNull ServerProfilePlugin plugin;
    private Essentials essentials;

    public EssentialsImporter(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void getEssentials(ServerLoadEvent event) {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (plugin == null) {
            return;
        }
        this.essentials = (Essentials) plugin;
    }

    @EventHandler
    private void importOnJoin(PlayerJoinEvent event) {
        if (essentials == null) {
            return;
        }
        Player player = event.getPlayer();
        User user = plugin.getUserManager().getUser(player);
        com.earth2me.essentials.User essentialsUser = essentials.getUser(player);

        // Import all homes
        for (String homeName : essentialsUser.getHomes()) {
            Location location = essentialsUser.getHome(homeName);
            user.getProfile().addHome(homeName, location);
            plugin.logger()
                  .info("Imported home \"{}\" for player {} at world={}, {} {} {}",
                        homeName,
                        player.getName(),
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ());
        }
    }
}
