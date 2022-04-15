package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import vekster.lightanticheat.api.LacFlagEvent;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightAntiCheatIntegrationConsumer extends AntiCheatIntegration {

    private static final String NUKER_PREFIX = "nuker";
    private static final Duration FAST_BREAK_FORGIVENESS = Duration.of(30, ChronoUnit.SECONDS);

    private final Map<UUID, ZonedDateTime> lastFastBreakTime = Collections.synchronizedMap(new HashMap<>());
    private final @NotNull FastBreakStorage.BrokenBlockConsumer brokenBlockConsumer;

    public LightAntiCheatIntegrationConsumer(@NotNull FastBreakStorage.BrokenBlockConsumer brokenBlockConsumer) throws
                                                                                                                NoSuchMethodException {
        super();
        this.brokenBlockConsumer = brokenBlockConsumer;
    }

    @Override
    public void onBroken(@NotNull Block block, @NotNull Player player) {
        lastFastBreakTime.put(player.getUniqueId(), ZonedDateTime.now());
        brokenBlockConsumer.onBroken(block, player);
    }

    @EventHandler(ignoreCancelled = true)
    private void disableFastBreakCheck(LacFlagEvent event) {
        // Ignore other flags
        if (!event.getCheckType().toLowerCase().startsWith(NUKER_PREFIX)) {
            return;
        }
        ZonedDateTime dateTime = lastFastBreakTime.get(event.getPlayer().getUniqueId());
        if (dateTime == null) {
            return;
        }
        // If inside the fastbreak forgiveness window, cancel the flag
        if (dateTime.isAfter(ZonedDateTime.now().minus(FAST_BREAK_FORGIVENESS))) {
            event.setCancelled(true);
        }
    }
}
