package com.rafaelsms.potocraft.combatserver;

import com.rafaelsms.potocraft.combatserver.listeners.CombatDrops;
import com.rafaelsms.potocraft.combatserver.listeners.CombatGameRules;
import com.rafaelsms.potocraft.combatserver.listeners.EquipmentManager;
import com.rafaelsms.potocraft.combatserver.listeners.UserManager;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class CombatServerPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;

    private final @NotNull UserManager userManager;

    public CombatServerPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(userManager, this);
        getServer().getPluginManager().registerEvents(new CombatDrops(this), this);
        getServer().getPluginManager().registerEvents(new CombatGameRules(this), this);
        getServer().getPluginManager().registerEvents(new EquipmentManager(this), this);
        //getServer().getPluginManager().registerEvents(new TestEquipmentListener(this), this);

        logger().info("CombatServer enabled");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        logger().info("CombatServer disabled");
    }

    public @NotNull Logger logger() {
        return getSLF4JLogger();
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull Database getDatabase() {
        return database;
    }
}
