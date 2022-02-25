package com.rafaelsms.potocraft.pet.player;

import com.rafaelsms.potocraft.pet.PetPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class User {

    private final @NotNull PetPlugin plugin;

    private final @NotNull Player player;
    private final @NotNull Profile profile;

    public User(@NotNull PetPlugin plugin, @NotNull Player player, @NotNull Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Profile getProfile() {
        return profile;
    }
}
