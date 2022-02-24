package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.protection.Region;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionManager implements Listener {

    private final @NotNull Map<UUID, Region> regions = new HashMap<>();

    private final @NotNull BlockProtectionPlugin plugin;

    public ProtectionManager(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }
}
