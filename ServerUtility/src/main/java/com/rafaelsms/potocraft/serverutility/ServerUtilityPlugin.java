package com.rafaelsms.potocraft.serverutility;

import club.minnced.discord.webhook.WebhookClient;
import com.rafaelsms.potocraft.BasePlugin;
import com.rafaelsms.potocraft.serverutility.commands.AnvilCommand;
import com.rafaelsms.potocraft.serverutility.commands.EnchantCommand;
import com.rafaelsms.potocraft.serverutility.commands.EnderchestCommand;
import com.rafaelsms.potocraft.serverutility.commands.FlightCommand;
import com.rafaelsms.potocraft.serverutility.commands.GameModeCommand;
import com.rafaelsms.potocraft.serverutility.commands.KillCommand;
import com.rafaelsms.potocraft.serverutility.commands.NearCommand;
import com.rafaelsms.potocraft.serverutility.commands.PlayerTimeCommand;
import com.rafaelsms.potocraft.serverutility.commands.PlayerWeatherCommand;
import com.rafaelsms.potocraft.serverutility.commands.SendDiscordhookCommand;
import com.rafaelsms.potocraft.serverutility.commands.SuicideCommand;
import com.rafaelsms.potocraft.serverutility.commands.VipCommand;
import com.rafaelsms.potocraft.serverutility.commands.WorkbenchCommand;
import com.rafaelsms.potocraft.serverutility.listeners.DamageEffects;
import com.rafaelsms.potocraft.serverutility.listeners.DamageModifier;
import com.rafaelsms.potocraft.serverutility.listeners.ExperienceModifier;
import com.rafaelsms.potocraft.serverutility.listeners.FirstSpawnPointListener;
import com.rafaelsms.potocraft.serverutility.listeners.FlightListener;
import com.rafaelsms.potocraft.serverutility.listeners.HeadDropper;
import com.rafaelsms.potocraft.serverutility.listeners.HideMessagesListener;
import com.rafaelsms.potocraft.serverutility.listeners.LavaListener;
import com.rafaelsms.potocraft.serverutility.listeners.NightVisionListener;
import com.rafaelsms.potocraft.serverutility.listeners.PlayerLogging;
import com.rafaelsms.potocraft.serverutility.listeners.ProjectileListener;
import com.rafaelsms.potocraft.serverutility.listeners.PurpurPermissionChecker;
import com.rafaelsms.potocraft.serverutility.listeners.QuickBreakOreListener;
import com.rafaelsms.potocraft.serverutility.listeners.QuickBreakTreeListener;
import com.rafaelsms.potocraft.serverutility.listeners.QuickReplantListener;
import com.rafaelsms.potocraft.serverutility.listeners.RainyNightListener;
import com.rafaelsms.potocraft.serverutility.listeners.ScoreboardListener;
import com.rafaelsms.potocraft.serverutility.listeners.VillagerListener;
import com.rafaelsms.potocraft.serverutility.listeners.WorldBorderApplier;
import com.rafaelsms.potocraft.serverutility.listeners.WorldConfigurationApplier;
import com.rafaelsms.potocraft.serverutility.listeners.WorldGameRuleApplier;
import com.rafaelsms.potocraft.serverutility.listeners.WorldGuardNoDeathDropsFlag;
import com.rafaelsms.potocraft.serverutility.listeners.WorldGuardNoEquipmentDamageFlag;
import com.rafaelsms.potocraft.serverutility.tasks.SyncWorldTimeTask;
import com.rafaelsms.potocraft.serverutility.tasks.TimedPVPTask;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

public class ServerUtilityPlugin extends BasePlugin {

    private final @NotNull Configuration configuration;
    private @Nullable WebhookClient webhookClient = null;

    // Custom WorldGuard flags
    private WorldGuardNoEquipmentDamageFlag noEquipmentDamageFlag = null;
    private WorldGuardNoDeathDropsFlag noDeathDropsFlag = null;

    public ServerUtilityPlugin() throws IOException {
        this.configuration = new Configuration(this);
    }

    @Override
    public void onLoad() {
        try {
            this.noEquipmentDamageFlag = new WorldGuardNoEquipmentDamageFlag(this);
            this.noDeathDropsFlag = new WorldGuardNoDeathDropsFlag(this);
        } catch (NoClassDefFoundError exception) {
            logger().error("Failed to initialize WorldGuard's custom flags: Class \"{}\" not found",
                           exception.getMessage());
        }
    }

    @Override
    public void onEnable() {
        // Create webhook
        if (!configuration.getWebhookUrl().isBlank()) {
            webhookClient = WebhookClient.withUrl(configuration.getWebhookUrl());
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new WorldGameRuleApplier(this), this);
        getServer().getPluginManager().registerEvents(new HideMessagesListener(this), this);
        getServer().getPluginManager().registerEvents(new LavaListener(this), this);
        getServer().getPluginManager().registerEvents(new ExperienceModifier(this), this);
        getServer().getPluginManager().registerEvents(new RainyNightListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageModifier(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLogging(this), this);
        getServer().getPluginManager().registerEvents(new QuickBreakTreeListener(this), this);
        getServer().getPluginManager().registerEvents(new QuickBreakOreListener(this), this);
        getServer().getPluginManager().registerEvents(new QuickReplantListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageEffects(this), this);
        getServer().getPluginManager().registerEvents(new WorldConfigurationApplier(this), this);
        getServer().getPluginManager().registerEvents(new HeadDropper(this), this);
        getServer().getPluginManager().registerEvents(new WorldBorderApplier(this), this);
        getServer().getPluginManager().registerEvents(new FlightListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileListener(this), this);
        getServer().getPluginManager().registerEvents(new NightVisionListener(), this);
        getServer().getPluginManager().registerEvents(new FirstSpawnPointListener(this), this);
        getServer().getPluginManager().registerEvents(new PurpurPermissionChecker(this), this);
        getServer().getPluginManager().registerEvents(new ScoreboardListener(this), this);
        if (noEquipmentDamageFlag != null) {
            getServer().getPluginManager().registerEvents(noEquipmentDamageFlag, this);
        }
        if (noDeathDropsFlag != null) {
            getServer().getPluginManager().registerEvents(noDeathDropsFlag, this);
        }

        // Run world sync task
        getServer().getScheduler().runTaskTimer(this, new SyncWorldTimeTask(this), 5L, 5L);
        getServer().getScheduler().runTaskTimer(this, new TimedPVPTask(this), 10L, 10L);

        // Register commands
        registerCommand("anvil", new AnvilCommand(this));
        registerCommand("enderchest", new EnderchestCommand(this));
        registerCommand("workbench", new WorkbenchCommand(this));
        registerCommand("playertime", new PlayerTimeCommand(this));
        registerCommand("playerweather", new PlayerWeatherCommand(this));
        registerCommand("gamemode", new GameModeCommand(this));
        registerCommand("enchant", new EnchantCommand(this));
        registerCommand("suicide", new SuicideCommand(this));
        registerCommand("kill", new KillCommand(this));
        registerCommand("fly", new FlightCommand(this));
        registerCommand("sendwebhook", new SendDiscordhookCommand(this));
        registerCommand("near", new NearCommand(this));
        registerCommand("vip", new VipCommand(this));

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

    public @NotNull Optional<WebhookClient> getWebhookClient() {
        return Optional.ofNullable(webhookClient);
    }

    public boolean isVulcanIntegrationAvailable() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Vulcan");
        return plugin != null && plugin.isEnabled();
    }

    public boolean isLACIntegrationAvailable() {
        Plugin plugin = getServer().getPluginManager().getPlugin("LightAntiCheat");
        return plugin != null && plugin.isEnabled();
    }
}
