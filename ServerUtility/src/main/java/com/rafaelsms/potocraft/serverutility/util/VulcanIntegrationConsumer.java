package com.rafaelsms.potocraft.serverutility.util;

import me.frep.vulcan.api.event.VulcanFlagEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VulcanIntegrationConsumer implements FastBreakStorage.BrokenBlockConsumer, Listener {

    private static final String FAST_BREAK_CATEGORY = "fastbreak";
    private static final Duration FAST_BREAK_FORGIVENESS = Duration.of(30, ChronoUnit.SECONDS);

    private final Map<UUID, ZonedDateTime> lastFastBreakTime = Collections.synchronizedMap(new HashMap<>());
    private final @NotNull FastBreakStorage.BrokenBlockConsumer brokenBlockConsumer;

    public VulcanIntegrationConsumer(@NotNull FastBreakStorage.BrokenBlockConsumer brokenBlockConsumer) {
        this.brokenBlockConsumer = brokenBlockConsumer;
    }

    @Override
    public void onBroken(@NotNull Block block, @NotNull Player player) {
        lastFastBreakTime.put(player.getUniqueId(), ZonedDateTime.now());
        brokenBlockConsumer.onBroken(block, player);
    }

    @EventHandler(ignoreCancelled = true)
    private void disableFastBreakCheck(VulcanFlagEvent event) {
        // Ignore other flags
        if (!event.getCheck().getCategory().equalsIgnoreCase(FAST_BREAK_CATEGORY)) {
            return;
        }
        ZonedDateTime dateTime = lastFastBreakTime.get(event.getPlayer().getUniqueId());
        if (dateTime == null) {
            return;
        }
        // If inside the fastbreak forgiveness window, cancel the flag
        if (ZonedDateTime.now().minus(FAST_BREAK_FORGIVENESS).isBefore(dateTime)) {
            event.setCancelled(true);
        }
    }
}
