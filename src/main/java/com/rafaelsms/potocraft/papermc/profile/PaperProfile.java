package com.rafaelsms.potocraft.papermc.profile;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.common.profile.Profile;
import com.rafaelsms.potocraft.common.util.Location;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperProfile extends Profile {

    private final @NotNull PaperPlugin plugin;
    private final @NotNull Player player;

    protected PaperProfile(@NotNull PaperPlugin plugin, @NotNull Player player) {
        super(plugin, player.getUniqueId(), player.getName());
        this.plugin = plugin;
        this.player = player;
    }

    public PaperProfile(@NotNull PaperPlugin plugin, @NotNull Player player, @NotNull Document document) {
        super(plugin, document);
        this.plugin = plugin;
        this.player = player;
    }

    public void setQuitLocation() {
        super.setLastLocation(Location.fromPlayer(plugin.getSettings().getServerName(), player.getLocation()));
    }
}
