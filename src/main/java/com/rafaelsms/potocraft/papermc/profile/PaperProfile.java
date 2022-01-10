package com.rafaelsms.potocraft.papermc.profile;

import com.rafaelsms.potocraft.common.profile.Location;
import com.rafaelsms.potocraft.common.profile.Profile;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperProfile extends Profile {

    private final @NotNull PaperPlugin plugin;

    protected PaperProfile(@NotNull PaperPlugin plugin, @NotNull Player player) {
        super(plugin, player.getUniqueId(), player.getName());
        this.plugin = plugin;
    }

    public PaperProfile(@NotNull PaperPlugin plugin, @NotNull Document document) {
        super(plugin, document);
        this.plugin = plugin;
    }

    public void setQuitLocation(@Nullable org.bukkit.Location location) {
        super.setLastLocation(Location.fromPlayer(plugin.getSettings().getServerName(), location));
    }
}
