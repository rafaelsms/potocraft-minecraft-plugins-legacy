package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ProtectionListener implements Listener {

    private final @NotNull BlockProtectionPlugin plugin;

    public ProtectionListener(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }
}
