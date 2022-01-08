package com.rafaelsms.potocraft.papermc;

import com.rafaelsms.potocraft.common.CommonServer;
import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.Plugin;
import com.rafaelsms.potocraft.papermc.listeners.ProfileUpdater;
import com.rafaelsms.potocraft.papermc.util.PaperDatabase;
import com.rafaelsms.potocraft.common.util.PluginType;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class PaperPlugin extends JavaPlugin implements Plugin {

    private final @NotNull CommonServer commonServer = new PaperServer(this);

    private @Nullable PaperSettings settings = null;
    private @Nullable PaperDatabase database = null;

    @Override
    public void onEnable() {
        try {
            this.settings = new PaperSettings(this);
            this.database = new PaperDatabase(this);
        } catch (Exception exception) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register permissions on Bukkit
        for (String permission : Permissions.values) {
            Permission bukkitPermission = getServer().getPluginManager().getPermission(permission);
            if (bukkitPermission == null) {
                getServer().getPluginManager().addPermission(new Permission(permission));
            }
        }

        getServer().getPluginManager().registerEvents(new ProfileUpdater(this), this);

//        getServer().getMessenger().registerIncomingPluginChannel(this, "potocraft:server", new PluginMessageListener() {
//            @Override
//            public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
//                logger().info("channel %s, player %s, message: %s".formatted(channel, player.getName(), message));
//            }
//        });
//        getServer().getMessenger().registerOutgoingPluginChannel(this, "potocraft:server");
//        getServer().getScheduler().runTaskTimer(
//                this,
//                () -> {
//                    getServer().sendPluginMessage(this, "potocraft:server", new byte[]{'B', 'C'});
//                    Optional<? extends Player> first = getServer().getOnlinePlayers().stream().findFirst();
//                    first.ifPresent(player -> player.sendPluginMessage(this, "potocraft:server", new byte[]{'A'}));
//                }, 40, 40
//        );
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public @NotNull CommonServer getCommonServer() {
        return commonServer;
    }

    @Override
    public @NotNull PluginType getPluginType() {
        return PluginType.PAPER;
    }

    @Override
    public @NotNull PaperSettings getSettings() {
        assert settings != null;
        return settings;
    }

    @Override
    public @NotNull Logger logger() {
        return getSLF4JLogger();
    }

    @Override
    public @NotNull PaperDatabase getDatabase() {
        assert database != null;
        return database;
    }
}
