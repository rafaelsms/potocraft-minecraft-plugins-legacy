package com.rafaelsms.potocraft.plugin;

import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.player.BaseUserManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class BaseJavaPlugin<User, Profile, Database extends BaseDatabase, Configuration extends BaseConfiguration>
        extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getPluginManager().registerEvents(getUserManager().getListener(), this);
        executeOnEnable();
        logger().info("{} enabled!", getPluginName());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        super.onDisable();
        executeOnDisable();
        getServer().getScheduler().cancelTasks(this);
        getDatabase().close();
        logger().info("{} disabled.", getPluginName());
    }

    public @NotNull Logger logger() {
        return getSLF4JLogger();
    }

    protected void registerCommand(@NotNull String commandName, @NotNull CommandExecutor executor) {
        PluginCommand pluginCommand = getServer().getPluginCommand(commandName);
        assert pluginCommand != null;
        pluginCommand.setExecutor(executor);
        if (executor instanceof TabCompleter tabCompleter) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    protected abstract @NotNull String getPluginName();

    protected abstract void executeOnEnable();

    protected abstract void executeOnDisable();

    public abstract @NotNull Configuration getConfiguration();

    public abstract @NotNull BaseUserManager<User, Profile> getUserManager();

    public abstract @NotNull Database getDatabase();
}
