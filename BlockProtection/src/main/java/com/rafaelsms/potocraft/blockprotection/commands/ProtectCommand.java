package com.rafaelsms.potocraft.blockprotection.commands;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.Permissions;
import com.rafaelsms.potocraft.blockprotection.players.Profile;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.blockprotection.protection.Selection;
import com.rafaelsms.potocraft.blockprotection.util.WorldGuardUtil;
import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ProtectCommand implements CommandExecutor, TabCompleter {

    /*
     * /proteger expandir
     * /proteger criar (nome região)
     * /proteger membro (nome jogador)
     * /proteger dono (nome jogador)
     * Not yet:
     * /proteger apagar (nome região)
     */

    private final @NotNull BlockProtectionPlugin plugin;

    public ProtectCommand(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnlyCommand());
            return true;
        }
        if (!sender.hasPermission(Permissions.PROTECT_COMMAND)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        // Check if world is protected
        World world = player.getWorld();
        if (!plugin.getConfiguration().getProtectedWorlds().contains(world.getName())) {
            sender.sendMessage(plugin.getConfiguration().getSelectionWorldNotProtected());
            return true;
        }

        // Check if region manager is available
        Optional<WorldGuard> worldGuardInstance = plugin.getWorldGuardInstance();
        if (worldGuardInstance.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
            return true;
        }
        User user = plugin.getUserManager().getUser(player);
        Optional<RegionManager> regionManager = plugin.getRegionManager(player);
        if (regionManager.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
            return true;
        }

        // Clear selection if on another world
        if (user.getSelection().isPresent()) {
            Optional<World> worldOptional = user.getSelection().get().getSelectionWorld();
            if (worldOptional.isPresent() && !Objects.equals(worldOptional.get().getUID(), world.getUID())) {
                sender.sendMessage(plugin.getConfiguration().getSelectionOnDifferentWorld());
                user.setSelection(null);
            }
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfiguration().getProtectHelp());
            return true;
        }

        if (args[0].equalsIgnoreCase("expandir")) {
            if (!sender.hasPermission(Permissions.PROTECT_COMMAND_EXPAND)) {
                sender.sendMessage(plugin.getServer().getPermissionMessage());
                return true;
            }

            // Make selection new
            Optional<Selection> selectionOptional = user.getSelection();
            if (selectionOptional.isEmpty() || selectionOptional.get().getExistingProtectedRegion().isEmpty()) {
                // Start selection to edit region
                Optional<ProtectedRegion> regionOptional = WorldGuardUtil.getProtectedRegion(plugin, player, true);
                if (regionOptional.isEmpty()) {
                    return true;
                }
                ProtectedRegion protectedRegion = regionOptional.get();
                user.setSelection(new Selection(plugin, user, protectedRegion));
                sender.sendMessage(plugin.getConfiguration().getProtectExpandHelp());
            } else {
                Selection selection = selectionOptional.get();
                if (!user.hasEnoughVolume(selection.getVolumeCost())) {
                    sender.sendMessage(plugin.getConfiguration().getSelectionVolumeExceeded());
                    return true;
                }

                ProtectedRegion existingRegion = selection.getExistingProtectedRegion().get();
                Optional<ProtectedRegion> newRegion = selection.getProtectedRegion(existingRegion.getId(), false);
                if (newRegion.isEmpty()) {
                    sender.sendMessage(plugin.getConfiguration().getSelectionRequired());
                    return true;
                }
                newRegion.get().copyFrom(existingRegion);

                try {
                    regionManager.get().addRegion(newRegion.get());
                    regionManager.get().saveChanges();
                    user.setSelection(null);
                    user.consumeVolume(selection.getVolumeCost());
                } catch (StorageException exception) {
                    // Revert region replacement
                    regionManager.get().removeRegion(newRegion.get().getId());
                    regionManager.get().addRegion(existingRegion);
                    plugin.logger().error("Failed to save changes on WorldGuard:", exception);
                    player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
                    return true;
                }
                sender.sendMessage(plugin.getConfiguration().getProtectRegionExpanded());
            }
            return true;
        } else if (args[0].equalsIgnoreCase("deletar")) {
            Optional<ProtectedRegion> protectedRegion = WorldGuardUtil.getProtectedRegion(plugin, player, false, true);
            if (protectedRegion.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getProtectDeleteHelp());
                return true;
            }

            if (!user.getProfile().isRegionOwner(protectedRegion.get().getId())) {
                player.sendMessage(plugin.getConfiguration().getProtectOnlyCreatorCanOperate());
                return true;
            }

            Set<ProtectedRegion> removedRegions =
                    regionManager.get().removeRegion(protectedRegion.get().getId(), RemovalStrategy.REMOVE_CHILDREN);
            if (removedRegions == null) {
                player.sendMessage(plugin.getConfiguration().getRegionNotFound());
                return true;
            }

            try {
                regionManager.get().saveChanges();
                // Give back volume but using the permission-based limiter
                user.incrementVolume(user.getDeletionPayback() * protectedRegion.get().volume());
                player.sendMessage(plugin.getConfiguration().getProtectRegionDeleted());
            } catch (StorageException exception) {
                // Revert change
                for (ProtectedRegion removedRegion : removedRegions) {
                    regionManager.get().addRegion(removedRegion);
                }
                plugin.logger().error("Failed to save changes on WorldGuard:", exception);
                player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
                return true;
            }
        } else if (args[0].equalsIgnoreCase("criar")) {
            if (!sender.hasPermission(Permissions.PROTECT_COMMAND_CREATE)) {
                sender.sendMessage(plugin.getServer().getPermissionMessage());
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(plugin.getConfiguration().getProtectCreateHelp());
                return true;
            }

            // Check if selection exists
            Optional<Selection> selectionOptional = user.getSelection();
            if (selectionOptional.isEmpty()) {
                sender.sendMessage(plugin.getConfiguration().getSelectionRequired());
                return true;
            }
            // Check volume cost
            if (!user.hasEnoughVolume(selectionOptional.get().getVolumeCost())) {
                sender.sendMessage(plugin.getConfiguration().getSelectionVolumeExceeded());
                return true;
            }

            // Check if region exists
            String regionName = TextUtil.normalizeString(args[1]);
            String regionId = "%s-%s".formatted(player.getUniqueId().toString(), regionName);
            if (!ProtectedRegion.isValidId(regionId)) {
                sender.sendMessage(plugin.getConfiguration().getProtectInvalidName());
                return true;
            }
            if (regionManager.get().getRegion(regionId) != null) {
                sender.sendMessage(plugin.getConfiguration().getProtectRegionAlreadyExists());
                return true;
            }

            // Make region
            Optional<ProtectedRegion> protectedRegion = selectionOptional.get().getProtectedRegion(regionId, false);
            if (protectedRegion.isEmpty()) {
                sender.sendMessage(plugin.getConfiguration().getSelectionRequired());
                return true;
            }
            protectedRegion.get().getOwners().addPlayer(player.getUniqueId());
            Optional<ProtectedRegion> baseRegion = plugin.getBaseRegion(world);
            if (baseRegion.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
                return true;
            }
            try {
                protectedRegion.get().setParent(baseRegion.get());
            } catch (ProtectedRegion.CircularInheritanceException exception) {
                throw new RuntimeException(exception);
            }

            // Complete region and save it
            try {
                regionManager.get().addRegion(protectedRegion.get());
                regionManager.get().saveChanges();
                user.setSelection(null);
                user.consumeVolume(protectedRegion.get().volume());
                user.getProfile().addCreatedRegion(regionName, regionId);
            } catch (StorageException exception) {
                regionManager.get().removeRegion(regionId);
                plugin.logger().error("Failed to save changes on WorldGuard:", exception);
                player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
                return true;
            }
            return true;
        } else if (args[0].equalsIgnoreCase("membro")) {
            if (!sender.hasPermission(Permissions.PROTECT_COMMAND_TOGGLE_MEMBER)) {
                sender.sendMessage(plugin.getServer().getPermissionMessage());
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(plugin.getConfiguration().getProtectToggleMemberHelp());
                return true;
            }
            togglePlayerOnRegion(player, args[1], false);
        } else if (args[0].equalsIgnoreCase("dono")) {
            if (!sender.hasPermission(Permissions.PROTECT_COMMAND_TOGGLE_OWNER)) {
                sender.sendMessage(plugin.getServer().getPermissionMessage());
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(plugin.getConfiguration().getProtectToggleOwnerHelp());
                return true;
            }
            togglePlayerOnRegion(player, args[1], true);
        } else if (args[0].equalsIgnoreCase("cancelar")) {
            user.setSelection(null);
            sender.sendMessage(plugin.getConfiguration().getProtectClearedSelection());
        } else {
            sender.sendMessage(plugin.getConfiguration().getProtectHelp());
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("criar", "expandir", "deletar", "membro", "dono", "cancelar");
        } else {
            return Util.convertList(plugin.getServer().getOnlinePlayers(), Player::getName);
        }
    }

    private void togglePlayerOnRegion(@NotNull Player player, @NotNull String playerName, boolean addAsOwner) {
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayerIfCached(playerName);
        if (offlinePlayer == null || offlinePlayer.getName() == null) {
            player.sendMessage(plugin.getConfiguration().getPlayerNotFound());
            return;
        }
        // Get region manager
        Optional<RegionManager> regionManager = plugin.getRegionManager(player);
        if (regionManager.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
            return;
        }

        // This already checks permission and warns player
        Optional<ProtectedRegion> protectedRegion = WorldGuardUtil.getProtectedRegion(plugin, player, true);
        if (protectedRegion.isEmpty()) {
            return;
        }

        try {
            Optional<Profile> profile = plugin.getDatabase().getProfile(offlinePlayer.getUniqueId());
            if (profile.isPresent() && profile.get().isRegionOwner(protectedRegion.get().getId())) {
                player.sendMessage(plugin.getConfiguration().getProtectCantToggleCreator());
                return;
            }
        } catch (Database.DatabaseException exception) {
            plugin.logger().error("Failed to fetch player profile:", exception);
            player.sendMessage(plugin.getConfiguration().getFailedToFetchProfile());
            return;
        }

        // Toggle player status
        boolean playerAdded;
        if (addAsOwner) {
            playerAdded = togglePlayer(protectedRegion.get().getOwners(), offlinePlayer.getUniqueId());
        } else {
            playerAdded = togglePlayer(protectedRegion.get().getMembers(), offlinePlayer.getUniqueId());
        }

        try {
            // Save and send message
            regionManager.get().saveChanges();
            Component message;
            if (playerAdded) {
                message = plugin.getConfiguration().getProtectPlayerAddedToRegion(offlinePlayer.getName());
            } else {
                message = plugin.getConfiguration().getProtectPlayerRemovedFromRegion(offlinePlayer.getName());
            }
            player.sendMessage(message);
        } catch (StorageException exception) {
            // Revert changes (toggle back)
            if (addAsOwner) {
                togglePlayer(protectedRegion.get().getOwners(), offlinePlayer.getUniqueId());
            } else {
                togglePlayer(protectedRegion.get().getMembers(), offlinePlayer.getUniqueId());
            }
            // Warn about failure
            plugin.logger().error("Failed to save changes to WorldGuard:", exception);
            player.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
        }
    }

    private boolean togglePlayer(@NotNull DefaultDomain defaultDomain, @NotNull UUID playerId) {
        if (defaultDomain.contains(playerId)) {
            defaultDomain.removePlayer(playerId);
            return false;
        } else {
            defaultDomain.addPlayer(playerId);
            return true;
        }
    }
}
