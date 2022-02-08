package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.serverutility.commands.AnvilCommand;
import com.rafaelsms.potocraft.serverutility.commands.EnchantCommand;
import com.rafaelsms.potocraft.serverutility.commands.EnderchestCommand;
import com.rafaelsms.potocraft.serverutility.commands.GameModeCommand;
import com.rafaelsms.potocraft.serverutility.commands.KillCommand;
import com.rafaelsms.potocraft.serverutility.commands.PlayerTimeCommand;
import com.rafaelsms.potocraft.serverutility.commands.PlayerWeatherCommand;
import com.rafaelsms.potocraft.serverutility.commands.SuicideCommand;
import com.rafaelsms.potocraft.serverutility.commands.VanishCommand;
import com.rafaelsms.potocraft.serverutility.commands.WorkbenchCommand;
import com.rafaelsms.potocraft.serverutility.listeners.DamageEffects;
import com.rafaelsms.potocraft.serverutility.listeners.DamageModifier;
import com.rafaelsms.potocraft.serverutility.listeners.ExperienceModifier;
import com.rafaelsms.potocraft.serverutility.listeners.HideMessagesListener;
import com.rafaelsms.potocraft.serverutility.listeners.LavaListener;
import com.rafaelsms.potocraft.serverutility.listeners.MendingNerfListener;
import com.rafaelsms.potocraft.serverutility.listeners.PlayerLogging;
import com.rafaelsms.potocraft.serverutility.listeners.QuickBreakOreListener;
import com.rafaelsms.potocraft.serverutility.listeners.QuickBreakTreeListener;
import com.rafaelsms.potocraft.serverutility.listeners.QuickReplantListener;
import com.rafaelsms.potocraft.serverutility.listeners.RainyNightListener;
import com.rafaelsms.potocraft.serverutility.listeners.VanishManager;
import com.rafaelsms.potocraft.serverutility.listeners.VillagerListener;
import com.rafaelsms.potocraft.serverutility.listeners.WorldGameRuleApplier;
import com.rafaelsms.potocraft.serverutility.tasks.SyncWorldTimeTask;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

public class ServerUtilityPlugin extends JavaPlugin {

    private final @NotNull Configuration configuration;
    private final @NotNull VanishManager manager;

    public ServerUtilityPlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.manager = new VanishManager(this);
    }

    @Override
    public void onEnable() {
        // Register listeners
        getServer().getPluginManager().registerEvents(manager, this);
        getServer().getPluginManager().registerEvents(new WorldGameRuleApplier(this), this);
        getServer().getPluginManager().registerEvents(new HideMessagesListener(this), this);
        getServer().getPluginManager().registerEvents(new LavaListener(this), this);
        getServer().getPluginManager().registerEvents(new MendingNerfListener(this), this);
        getServer().getPluginManager().registerEvents(new ExperienceModifier(this), this);
        getServer().getPluginManager().registerEvents(new RainyNightListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageModifier(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLogging(this), this);
        getServer().getPluginManager().registerEvents(new QuickBreakTreeListener(this), this);
        getServer().getPluginManager().registerEvents(new QuickBreakOreListener(this), this);
        getServer().getPluginManager().registerEvents(new QuickReplantListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageEffects(this), this);

        // Run world sync task
        getServer().getScheduler().runTaskTimer(this, new SyncWorldTimeTask(this), 10L, 10L);

        // Register commands
        registerCommand("anvil", new AnvilCommand(this));
        registerCommand("enderchest", new EnderchestCommand(this));
        registerCommand("workbench", new WorkbenchCommand(this));
        registerCommand("playertime", new PlayerTimeCommand(this));
        registerCommand("playerweather", new PlayerWeatherCommand(this));
        registerCommand("vanish", new VanishCommand(this));
        registerCommand("gamemode", new GameModeCommand(this));
        registerCommand("enchant", new EnchantCommand(this));
        registerCommand("suicide", new SuicideCommand(this));
        registerCommand("kill", new KillCommand(this));

        logger().info("ServerUtility enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("ServerUtility disabled!");
    }

    public @NotNull Configuration getConfiguration() {
        return configuration;
    }

    public @NotNull VanishManager getVanishManager() {
        return manager;
    }

    public Logger logger() {
        return getSLF4JLogger();
    }

    private void registerCommand(@NotNull String command, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(command);
        if (pluginCommand == null) {
            throw new IllegalStateException("Couldn't find command %s. Make sure it is on plugin.yml".formatted(command));
        }
        pluginCommand.setExecutor(executor);
        if (executor instanceof TabCompleter tabCompleter) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
    }
}
