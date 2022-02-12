package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class UserManager implements Listener {

    private final @NotNull CombatServerPlugin plugin;

    public UserManager(@NotNull CombatServerPlugin plugin) {
        this.plugin = plugin;
    }
}
