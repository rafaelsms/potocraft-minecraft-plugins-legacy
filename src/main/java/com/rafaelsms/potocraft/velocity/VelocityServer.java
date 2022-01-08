package com.rafaelsms.potocraft.velocity;

import com.rafaelsms.potocraft.common.CommonServer;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;

public class VelocityServer implements CommonServer {

    private final @NotNull ProxyServer server;
    private final @NotNull Path dataDirectory;

    public VelocityServer(@NotNull ProxyServer server, @NotNull Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public File getConfigurationFile() {
        File dataDirectory = this.dataDirectory.toFile();
        if (!dataDirectory.exists() && !dataDirectory.mkdir())
            throw new IllegalStateException("Couldn't create data folder");
        return new File(dataDirectory, "config.json");
    }

    @Override
    public void shutdown() {
        server.shutdown(Component.text("PotoCraft Proxy plugin required shutdown."));
    }
}
