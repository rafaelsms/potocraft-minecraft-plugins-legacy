package com.rafaelsms.potocraft;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BasePlugin extends JavaPlugin implements com.rafaelsms.potocraft.Logger {

    public @NotNull Logger logger() {
        return super.getSLF4JLogger();
    }

    protected void registerCommand(@NotNull String name, @NotNull CommandExecutor executor) {
        if (executor instanceof TabCompleter tabCompleter) {
            registerCommand(name, executor, tabCompleter);
        } else {
            registerCommand(name, executor, null);
        }
    }

    protected void registerCommand(@NotNull String name,
                                   @NotNull CommandExecutor executor,
                                   @Nullable TabCompleter tabCompleter) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            throw new IllegalStateException("Command couldn't be registered: %s".formatted(name));
        }
        pluginCommand.setExecutor(executor);
        if (tabCompleter != null) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
    }
}
