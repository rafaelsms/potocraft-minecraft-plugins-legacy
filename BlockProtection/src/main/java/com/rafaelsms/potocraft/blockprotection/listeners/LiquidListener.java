package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class LiquidListener implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    public LiquidListener(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }
}
