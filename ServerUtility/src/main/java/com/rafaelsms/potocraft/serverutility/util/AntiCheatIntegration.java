package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.event.Listener;

public abstract class AntiCheatIntegration implements Listener, FastBreakStorage.BrokenBlockConsumer {

    @SuppressWarnings("RedundantThrows")
    public AntiCheatIntegration() throws NoSuchMethodException {
    }
}
