package com.rafaelsms.potocraft.velocity;

import com.google.inject.Inject;
import com.rafaelsms.potocraft.CommonServer;
import com.rafaelsms.potocraft.util.PlayerType;
import com.rafaelsms.potocraft.util.PluginType;
import com.rafaelsms.potocraft.velocity.commands.ChangePinCommand;
import com.rafaelsms.potocraft.velocity.commands.LoginCommand;
import com.rafaelsms.potocraft.velocity.commands.RegisterCommand;
import com.rafaelsms.potocraft.velocity.database.VelocityDatabase;
import com.rafaelsms.potocraft.velocity.listeners.OfflineLoginChecker;
import com.rafaelsms.potocraft.velocity.listeners.ProfileUpdater;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Plugin(id = "potocraft-proxy", name = "PotoCraft Proxy", version = "0.1", url = "https://potocraft.com/",
        description = "Custom plugin for PotoCraft", authors = {"rafaelsms"},
        dependencies = {@Dependency(id = "floodgate")})
public class VelocityPlugin implements com.rafaelsms.potocraft.Plugin {

    private final @NotNull ProxyServer server;
    private final @NotNull CommonServer commonServer;
    private final @NotNull Logger logger;

    private @Nullable VelocitySettings settings = null;
    private @Nullable VelocityDatabase database = null;

    @Inject
    public VelocityPlugin(@NotNull ProxyServer server, @NotNull Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.commonServer = new VelocityServer(server, dataDirectory);
        this.logger = logger;
        try {
            this.settings = new VelocitySettings(this);
        } catch (Exception exception) {
            logger.error("Failed to create velocity settings: %s".formatted(exception.getLocalizedMessage()));
            exception.printStackTrace();
            return;
        }
        this.database = new VelocityDatabase(this);
        logger.info("PotoCraft Proxy constructed");
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

        // Register listeners
        getProxyServer().getEventManager().register(this, new OfflineLoginChecker(this));
        getProxyServer().getEventManager().register(this, new ProfileUpdater(this));

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

    private void retrievePlayerType(@NotNull UUID playerId,
                                   @Nullable Consumer<PlayerType> playerTypeConsumer,
                                   @Nullable Runnable typeNotFoundRunnable,
                                   @Nullable Consumer<Exception> exceptionConsumer) {
        try {
            Optional<PlayerType> playerTypeOptional = getPlayerType(playerId);
            if (playerTypeOptional.isPresent()) {
                debug("Found player type for player %s".formatted(playerId.toString()));
                if (playerTypeConsumer == null) return;
                playerTypeConsumer.accept(playerTypeOptional.get());
            } else {
                debug("Could not find player type for player %s".formatted(playerId.toString()));
                if (typeNotFoundRunnable == null) return;
                typeNotFoundRunnable.run();
            }
        } catch (Exception exception) {
            if (exceptionConsumer == null) return;
            exceptionConsumer.accept(exception);
        }
    }

    public Optional<PlayerType> getPlayerType(@NotNull Player player) {
        if (FloodgateApi.getInstance() != null) {
            for (FloodgatePlayer floodgatePlayer : FloodgateApi.getInstance().getPlayers()) {
                if (floodgatePlayer.getJavaUsername().equalsIgnoreCase(player.getUsername())) {
                    return Optional.of(PlayerType.FLOODGATE_PLAYER);
                }
            }
        } else {
            // Always empty on Floodgate unavailable
            return Optional.empty();
        }
        return Optional.of(player.isOnlineMode() ? PlayerType.ONLINE_PLAYER : PlayerType.OFFLINE_PLAYER);
    }


    /**
     * Get the player connection type given player id.
     *
     * @param playerId player's UUID
     * @return null if player is not found or if floodgate is unavailable
     * @throws ClassNotFoundException if floodgate is unavailable
     */
    private @NotNull Optional<PlayerType> getPlayerType(@NotNull UUID playerId) throws ClassNotFoundException {
        if (FloodgateApi.getInstance() == null) {
            throw new ClassNotFoundException("Floodgate API is unavailable");
        }
        if (FloodgateApi.getInstance().isFloodgateId(playerId)) {
            return Optional.of(PlayerType.FLOODGATE_PLAYER);
        }
        Optional<Player> optionalPlayer = getProxyServer().getPlayer(playerId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            if (player.isOnlineMode()) {
                return Optional.of(PlayerType.ONLINE_PLAYER);
            } else {
                return Optional.of(PlayerType.OFFLINE_PLAYER);
            }
        }
        return Optional.empty();
    }
}
