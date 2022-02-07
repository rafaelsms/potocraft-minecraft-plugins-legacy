package com.rafaelsms.potocraft.serverutility.commands;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final @NotNull ServerUtilityPlugin plugin;

    public EnchantCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfiguration().getPlayerOnly());
            return true;
        }
        if (!sender.hasPermission(Permissions.COMMAND_ENCHANTMENT)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(plugin.getConfiguration().getEnchantHelp());
            return true;
        }

        String enchantmentString = args[0];
        Optional<@NotNull Enchantment> enchantmentOptional = TextUtil.closestMatch(Arrays.asList(Enchantment.values()),
                                                                                   enchantment -> enchantment.getKey()
                                                                                                             .getKey(),
                                                                                   enchantmentString);
        if (enchantmentOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getEnchantHelp());
            return true;
        }

        Enchantment enchantment = enchantmentOptional.get();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!enchantment.canEnchantItem(hand)) {
            player.sendMessage(plugin.getConfiguration().getEnchantCantEnchantItem());
            return true;
        }
        if (!sender.hasPermission(Permissions.COMMAND_ENCHANTMENT_CONFLICTING)) {
            for (Enchantment otherEnchantment : hand.getEnchantments().keySet()) {
                if (otherEnchantment.conflictsWith(enchantment)) {
                    player.sendMessage(plugin.getConfiguration().getEnchantCantEnchantItem());
                    return true;
                }
            }
        }

        int level = enchantment.getMaxLevel();
        if (args.length == 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(plugin.getConfiguration().getEnchantHelp());
                return true;
            }
        }
        if (level > enchantment.getMaxLevel()) {
            if (sender.hasPermission(Permissions.COMMAND_ENCHANTMENT_UNSAFE)) {
                hand.addUnsafeEnchantment(enchantment, level);
                player.getInventory().setItemInMainHand(hand);
                return true;
            }
            level = enchantment.getMaxLevel();
        }

        hand.addEnchantment(enchantment, level);
        player.getInventory().setItemInMainHand(hand);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (sender instanceof Player player) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            ArrayList<Enchantment> enchantments = new ArrayList<>();
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.canEnchantItem(hand)) {
                    enchantments.add(enchantment);
                }
            }
            Util.convertList(enchantments, enchantment -> enchantment.getKey().getKey().toLowerCase());
        }
        return Util.convertList(Arrays.asList(Enchantment.values()),
                                enchantment -> enchantment.getKey().getKey().toLowerCase());
    }
}
