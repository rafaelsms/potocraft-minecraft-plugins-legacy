package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.user.UserProfile;
import com.rafaelsms.potocraft.util.Location;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperUser extends UserProfile {

    private final @NotNull PaperPlugin plugin;
    private final @NotNull Player player;

    protected PaperUser(@NotNull PaperPlugin plugin, @NotNull Player player) {
        super(plugin, player.getUniqueId(), player.getName());
        this.plugin = plugin;
        this.player = player;
    }

    public PaperUser(@NotNull PaperPlugin plugin, @NotNull Player player, @NotNull Document document) {
        super(plugin, document);
        this.plugin = plugin;
        this.player = player;
    }

    public void setQuitLocation() {
        super.setLastLocation(Location.fromPlayer(plugin.getSettings().getServerName(), player.getLocation()));
    }
}
