package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PurpurEnderchestBlocker implements Listener {

    private final String MAXIMUM_ENDERCHEST_PERMISSION = "purpur.enderchest.rows.six";
    private final Set<String> enderchestPermissions = Set.of("purpur.enderchest.rows.one",
                                                             "purpur.enderchest.rows.two",
                                                             "purpur.enderchest.rows.three",
                                                             "purpur.enderchest.rows.four",
                                                             "purpur.enderchest.rows.five",
                                                             MAXIMUM_ENDERCHEST_PERMISSION);

    private final @NotNull ServerUtilityPlugin plugin;

    public PurpurEnderchestBlocker(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void preventJoin(AsyncPlayerPreLoginEvent event) {
        if (isMissingPermission(event.getUniqueId())) {
            Component message = plugin.getConfiguration().getFailedEnderchestKickMessage();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
        }
    }

    @EventHandler
    private void preventOpenInventory(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST &&
            isMissingPermission(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(plugin.getConfiguration().getFailedEnderchestInventoryMessage());
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void setPermissionToQuit(PlayerQuitEvent event) {
        if (isMissingPermission(event.getPlayer().getUniqueId()) &&
            !setTemporaryPermission(event.getPlayer().getUniqueId())) {
            plugin.logger()
                  .error("Player is missing enderchest permissions and couldn't set up temporary one: {} (UUID = {})",
                         event.getPlayer().getName(),
                         event.getPlayer().getUniqueId());
        }
    }

    private boolean isMissingPermission(@NotNull UUID playerId) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            CachedPermissionData permissionData = user.getCachedData().getPermissionData();
            for (String enderchestPermission : enderchestPermissions) {
                if (permissionData.checkPermission(enderchestPermission).asBoolean()) {
                    return false;
                }
            }
            return true;
        } catch (Throwable ignored) {
            return true;
        }
    }

    private boolean setTemporaryPermission(@NotNull UUID playerId) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            return user.data()
                       .add(PermissionNode.builder()
                                          .permission(MAXIMUM_ENDERCHEST_PERMISSION)
                                          .expiry(1, TimeUnit.HOURS)
                                          .build())
                       .wasSuccessful();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
