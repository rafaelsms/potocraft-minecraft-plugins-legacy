package com.rafaelsms.potocraft.serverprofile;

import com.rafaelsms.potocraft.serverprofile.commands.BackCommand;
import com.rafaelsms.potocraft.serverprofile.commands.CreateHomeCommand;
import com.rafaelsms.potocraft.serverprofile.commands.CreateWarpCommand;
import com.rafaelsms.potocraft.serverprofile.commands.DeleteHomeCommand;
import com.rafaelsms.potocraft.serverprofile.commands.DeleteWarpCommand;
import com.rafaelsms.potocraft.serverprofile.commands.HomeCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TeleportAcceptCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TeleportCancelCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TeleportCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TeleportDenyCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TeleportHereCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TeleportOfflineCommand;
import com.rafaelsms.potocraft.serverprofile.commands.TopCommand;
import com.rafaelsms.potocraft.serverprofile.commands.WarpCommand;
import com.rafaelsms.potocraft.serverprofile.commands.WorldCommand;
import com.rafaelsms.potocraft.serverprofile.commands.completers.PlayerNameCompleter;
import com.rafaelsms.potocraft.serverprofile.commands.completers.TeleportRequesterCompleter;
import com.rafaelsms.potocraft.serverprofile.commands.completers.WarpNameCompleter;
import com.rafaelsms.potocraft.serverprofile.listeners.ChatFormatter;
import com.rafaelsms.potocraft.serverprofile.listeners.CombatListener;
import com.rafaelsms.potocraft.serverprofile.listeners.EssentialsImporter;
import com.rafaelsms.potocraft.serverprofile.listeners.HardcoreListener;
import com.rafaelsms.potocraft.serverprofile.listeners.StatisticsListener;
import com.rafaelsms.potocraft.serverprofile.listeners.TotemLimiter;
import com.rafaelsms.potocraft.serverprofile.listeners.UserManager;
import com.rafaelsms.potocraft.serverprofile.listeners.WorldGuardNoEnteringInCombatFlag;
import com.rafaelsms.potocraft.serverprofile.listeners.WorldGuardShowMemberFlag;
import com.rafaelsms.potocraft.serverprofile.util.WorldGuardUtil;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class ServerProfilePlugin extends JavaPlugin {

    public static StateFlag ENTERING_IN_COMBAT_FLAG = null;
    public static StateFlag REQUIRE_INITIAL_MEMBER_DAMAGE = null;
    public static StateFlag SHOW_MEMBERS_FLAG = null;

    private final @NotNull Configuration configuration;
    private final @NotNull Database database;
    private final @NotNull UserManager userManager;

    public ServerProfilePlugin() throws IOException {
        this.configuration = new Configuration(this);
        this.database = new Database(this);
        this.userManager = new UserManager(this);

        // Register WorldGuard flags on startup
        ENTERING_IN_COMBAT_FLAG =
                WorldGuardUtil.registerFlag(this, "entering-while-in-combat", true, RegionGroup.ALL).orElseThrow();
        REQUIRE_INITIAL_MEMBER_DAMAGE =
                WorldGuardUtil.registerFlag(this, "require-member-hit-to-pvp", false, RegionGroup.ALL).orElseThrow();
        SHOW_MEMBERS_FLAG =
                WorldGuardUtil.registerFlag(this, "show-members-when-entering", false, RegionGroup.ALL).orElseThrow();
    }

    // TODO make exit proxy don't kill people (warn servers)
    // TODO set player name color with scoreboards
    // TODO /reportar
    // TODO store player data and movement on database temporarily? (death, teleport, chat, command)

    @Override
    public void onEnable() {
        // Register listeners
        getServer().getPluginManager().registerEvents(userManager.getListener(), this);
        getServer().getPluginManager().registerEvents(new ChatFormatter(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new StatisticsListener(this), this);
        getServer().getPluginManager().registerEvents(new TotemLimiter(this), this);
        getServer().getPluginManager().registerEvents(new EssentialsImporter(this), this);
        getServer().getPluginManager().registerEvents(new HardcoreListener(this), this);

        // Register WorldGuard handlers
        getWorldGuard().ifPresent(instance -> {
            SessionManager sessionManager = instance.getPlatform().getSessionManager();
            // No entering while in combat
            WorldGuardNoEnteringInCombatFlag.setPlugin(this);
            sessionManager.registerHandler(WorldGuardNoEnteringInCombatFlag.FACTORY, null);
            // Showing member list on enter region
            WorldGuardShowMemberFlag.setPlugin(this);
            sessionManager.registerHandler(WorldGuardShowMemberFlag.FACTORY, null);
        });

        // Register commands
        registerCommand("voltar", new BackCommand(this));

        registerCommand("criarcasa", new CreateHomeCommand(this));
        registerCommand("apagarcasa", new DeleteHomeCommand(this));
        registerCommand("casa", new HomeCommand(this));

        registerCommand("criarportal", new CreateWarpCommand(this));
        WarpNameCompleter warpNameCompleter = new WarpNameCompleter(this);
        registerCommand("apagarportal", new DeleteWarpCommand(this), warpNameCompleter);
        registerCommand("portal", new WarpCommand(this), warpNameCompleter);

        TeleportRequesterCompleter teleportRequesterCompleter = new TeleportRequesterCompleter(this);
        registerCommand("teleporteaceitar", new TeleportAcceptCommand(this), teleportRequesterCompleter);
        registerCommand("teleporterecusar", new TeleportDenyCommand(this), teleportRequesterCompleter);
        registerCommand("teleportecancelar", new TeleportCancelCommand(this));
        registerCommand("teleporte", new TeleportCommand(this), new PlayerNameCompleter(this, Permissions.TELEPORT));
        registerCommand("teleporteoffline", new TeleportOfflineCommand(this));
        registerCommand("teleporteaqui",
                        new TeleportHereCommand(this),
                        new PlayerNameCompleter(this, Permissions.TELEPORT_HERE));
        registerCommand("mundo", new WorldCommand(this));
        registerCommand("top", new TopCommand(this));

        logger().info("ServerProfile enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        logger().info("ServerProfile disabled!");
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

    public @NotNull Optional<WorldGuard> getWorldGuard() {
        return Optional.ofNullable(WorldGuard.getInstance());
    }

    public Logger logger() {
        return getSLF4JLogger();
    }

    private void registerCommand(@NotNull String name, @NotNull CommandExecutor executor) {
        if (executor instanceof TabCompleter tabCompleter) {
            registerCommand(name, executor, tabCompleter);
        } else {
            registerCommand(name, executor, null);
        }
    }

    private void registerCommand(@NotNull String name,
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
