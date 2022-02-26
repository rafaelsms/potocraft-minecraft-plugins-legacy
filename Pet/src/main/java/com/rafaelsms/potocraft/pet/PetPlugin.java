package com.rafaelsms.potocraft.pet;

import com.rafaelsms.potocraft.pet.commands.PetCommand;
import com.rafaelsms.potocraft.pet.listeners.DamageListener;
import com.rafaelsms.potocraft.pet.listeners.UserManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class PetPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;
    private NPCRegistry npcRegistry = null;

    public PetPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);
    }

    @Override
    public void onEnable() {
        this.npcRegistry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
        getServer().getPluginManager().registerEvents(userManager.getListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        registerCommand("pet", new PetCommand(this));
        logger().info("Pet enabled");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        logger().info("Pet disabled");
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

    public @NotNull UserManager getUserManager() {
        return userManager;
    }

    public @NotNull NPCRegistry getNpcRegistry() {
        return npcRegistry;
    }

    private void registerCommand(@NotNull String command, @NotNull CommandExecutor commandExecutor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);
        }
    }
}
