package com.rafaelsms.potocraft.blockprotection.commands;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.Permissions;
import com.rafaelsms.potocraft.blockprotection.players.User;
import com.rafaelsms.potocraft.blockprotection.protection.Selection;
import com.rafaelsms.potocraft.blockprotection.util.WorldGuardUtil;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ProtectCommand implements CommandExecutor, TabCompleter {

    /*
     * /proteger expandir
     * /proteger criar (nome região)
     * /proteger apagar (nome região)
     * /proteger membro (nome jogador)
     * /proteger dono (nome jogador)
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
        User user = plugin.getUserManager().getUser(player);
        Optional<RegionManager> regionManager = plugin.getRegionManager(player);
        if (regionManager.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getFailedToFetchRegions());
            return true;
        }

        if (args.length == 0) {
            // TODO HELP
            return true;
        }

        if (args[0].equalsIgnoreCase("expandir")) {
            // Start selection to edit region
            Optional<ProtectedRegion> regionOptional = WorldGuardUtil.getProtectedRegion(plugin, player, true);
            if (regionOptional.isEmpty()) {
                return true;
            }
            ProtectedRegion protectedRegion = regionOptional.get();
            user.setSelection(new Selection(plugin, user, protectedRegion));
        } else if (args[0].equalsIgnoreCase("criar")) {
            if (args.length != 2) {
                // TODO help criar
                return true;
            }

            Optional<Selection> selectionOptional = user.getSelection();
            if (selectionOptional.isEmpty()) {
                // TODO use uma (pá de ouro -> config) para selecionar
                return true;
            }

            // TODO pegar seleção
            return true;
        } else if (args[0].equalsIgnoreCase("apagar")) {
            if (args.length != 2) {
                // TODO help apagar + lista de regiões do usuário
                return true;
            }
            // TODO apagar região
        } else if (args[0].equalsIgnoreCase("membro")) {
            if (args.length != 2) {
                // TODO help membro
                return true;
            }
            // TODO adicionar membro
        } else if (args[0].equalsIgnoreCase("dono")) {
            if (args.length != 2) {
                // TODO help dono
                return true;
            }
            // TODO adicionar dono
        } else {
            // TODO HELP
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        return null;
    }
}
