package com.rafaelsms.potocraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public abstract class PaperPlugin extends JavaPlugin implements Plugin {

    @Override
    public Logger logger() {
        return getSLF4JLogger();
    }

    @Override
    public void shutdown() {
        logger().error("Server shutting down...");
        getServer().shutdown();
    }
}
