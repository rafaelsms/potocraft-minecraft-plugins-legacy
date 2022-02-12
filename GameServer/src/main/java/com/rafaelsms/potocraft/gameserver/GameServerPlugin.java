package com.rafaelsms.potocraft.gameserver;

import com.rafaelsms.potocraft.gameserver.uhc.UHCGameMode;
import com.rafaelsms.potocraft.gameserver.util.GameMode;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class GameServerPlugin extends JavaPlugin {

    private final ArrayList<GameMode> gameModes = new ArrayList<>();

    private final @NotNull Configuration configuration;

    public GameServerPlugin() throws IOException {
        this.configuration = new Configuration(this);
    }

    @Override
    public void onEnable() {
        gameModes.add(new UHCGameMode(this));

        logger().info("GameServer enabled");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        logger().info("GameServer disabled");
    }

    public @NotNull Logger logger() {
        return getSLF4JLogger();
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }
}
