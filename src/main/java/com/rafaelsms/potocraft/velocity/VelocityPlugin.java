package com.rafaelsms.potocraft.velocity;

import com.google.inject.Inject;
import com.rafaelsms.potocraft.common.CommonServer;
import com.rafaelsms.potocraft.common.util.DepedencyException;
import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.common.util.PluginType;
import com.rafaelsms.potocraft.velocity.commands.*;
import com.rafaelsms.potocraft.velocity.database.VelocityDatabase;
import com.rafaelsms.potocraft.velocity.listeners.*;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import com.rafaelsms.potocraft.velocity.user.VelocityUserManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "potocraft-proxy", name = "PotoCraft Proxy", version = "0.1", url = "https://potocraft.com/",
        description = "Custom plugin for PotoCraft", authors = {"rafaelsms"},
        dependencies = {@Dependency(id = "floodgate"), @Dependency(id = "luckperms", optional = true)})
public class VelocityPlugin implements com.rafaelsms.potocraft.common.Plugin<VelocityProfile, VelocityUser, Player> {

    private final @NotNull ProxyServer server;
    private final @NotNull CommonServer commonServer;
    private final @NotNull Logger logger;

    private final @Nullable VelocitySettings settings;
    private final @Nullable VelocityDatabase database;
    private final @Nullable VelocityUserManager userManager;

    @Inject
    public VelocityPlugin(@NotNull ProxyServer server, @NotNull Logger logger, @DataDirectory Path dataDirectory) throws Exception {
        this.server = server;
        this.commonServer = new VelocityServer(server, dataDirectory);
        this.logger = logger;
        try {
            this.settings = new VelocitySettings(this);
        } catch (Exception exception) {
            logger.error("Failed to create velocity settings: %s".formatted(exception.getLocalizedMessage()));
            exception.printStackTrace();
            throw exception;
        }
        this.database = new VelocityDatabase(this);
        this.userManager = new VelocityUserManager(this);
    }

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) {
        if (settings == null || database == null) {
            logger.error("PotoCraft Proxy failed to construct, will not initialize.");
            return;
        }

        // Register commands
        getProxyServer().getCommandManager().register("login", new LoginCommand(this), "l", "log");
        getProxyServer().getCommandManager().register("registrar", new RegisterCommand(this), "reg", "register");
        getProxyServer().getCommandManager().register("mudarsenha", new ChangePinCommand(this), "changepin", "changepassword", "mudarpin");
        getProxyServer().getCommandManager().register("report", new ReportCommand(this), "reportar");
        getProxyServer().getCommandManager().register("mensagem", new MessageCommand(this), "msg", "message", "dm", "pm", "tell");
        getProxyServer().getCommandManager().register("responder", new ReplyCommand(this), "reply", "r");

        // Register listeners
        getProxyServer().getEventManager().register(this, new OfflineLoginChecker(this));
        getProxyServer().getEventManager().register(this, new ProfileUpdater(this));
        getProxyServer().getEventManager().register(this, new ReportChecker(this));
        getProxyServer().getEventManager().register(this, new ChatController(this));
        getProxyServer().getEventManager().register(this, new UserListener(getUserManager()));

//        MinecraftChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.create("potocraft", "server");
//        getProxyServer().getChannelRegistrar().register(channelIdentifier);
//        getProxyServer().getEventManager().register(this, new Object() {
//            @Subscribe
//            private void onPluginMessage(PluginMessageEvent event) {
//                if (channelIdentifier.equals(event.getIdentifier())) {
//                    logger.info(Arrays.toString(event.getData()));
//                }
//            }
//        });
//        getProxyServer().getScheduler().buildTask(this, () -> {
//            getProxyServer().getServer("lobby").get().sendPluginMessage(channelIdentifier, new byte[]{'b', 'c'});
//            Optional<Player> first = getProxyServer().getAllPlayers().stream().findFirst();
//            first.ifPresent(player -> player.sendPluginMessage(channelIdentifier, new byte[]{'a'}));
//        }).repeat(2, TimeUnit.SECONDS).schedule();

        logger.info("PotoCraft Proxy initialized!");
    }

    @Subscribe
    private void onProxyReload(ProxyReloadEvent event) {
        if (settings == null || database == null) {
            logger.error("PotoCraft Proxy failed to construct, will not reload.");
            return;
        }
        try {
            this.settings.reloadFile();
        } catch (Exception exception) {
            logger.error("PotoCraft Proxy failed to reload configuration: %s".formatted(exception.getLocalizedMessage()));
            exception.printStackTrace();
            return;
        }
        logger.info("PotoCraft Proxy reloaded");
    }

    @Subscribe
    private void onProxyShutdown(ProxyShutdownEvent event) {
        server.getEventManager().unregisterListeners(this);

        if (settings == null || database == null) {
            return;
        }
        logger.info("PotoCraft Proxy shutdown");
    }

    public ProxyServer getProxyServer() {
        return server;
    }

    @Override
    public @NotNull CommonServer getCommonServer() {
        return commonServer;
    }

    @Override
    public @NotNull Logger logger() {
        return logger;
    }

    @Override
    public @NotNull PluginType getPluginType() {
        return PluginType.VELOCITY;
    }

    @Override
    public @NotNull VelocitySettings getSettings() {
        assert settings != null;
        return settings;
    }

    @Override
    public @NotNull VelocityDatabase getDatabase() {
        assert database != null;
        return database;
    }

    @Override
    public @NotNull VelocityUserManager getUserManager() {
        assert userManager != null;
        return userManager;
    }

    public PlayerType getPlayerType(@NotNull Player player) throws DepedencyException {
        FloodgateApi floodgate = getFloodgate();
        for (FloodgatePlayer floodgatePlayer : floodgate.getPlayers()) {
            if (floodgatePlayer.getJavaUsername().equalsIgnoreCase(player.getUsername())) {
                return PlayerType.FLOODGATE_PLAYER;
            }
        }
        if (player.isOnlineMode()) {
            return PlayerType.ONLINE_PLAYER;
        } else {
            return PlayerType.OFFLINE_PLAYER;
        }
    }
}
