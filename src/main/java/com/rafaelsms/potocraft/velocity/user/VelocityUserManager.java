package com.rafaelsms.potocraft.velocity.user;

import com.rafaelsms.potocraft.common.user.UserManager;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

public class VelocityUserManager extends UserManager<VelocityUser, Player> {

    private final @NotNull VelocityPlugin plugin;

    public VelocityUserManager(@NotNull VelocityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected VelocityUser makeUser(Player playerInstance) {
        return new VelocityUser(plugin, playerInstance);
    }
}
