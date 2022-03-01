package com.rafaelsms.potocraft.combatserver.commands;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import com.rafaelsms.potocraft.combatserver.Permissions;
import com.rafaelsms.potocraft.combatserver.util.InventoryContent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class KitCommand implements CommandExecutor {

    private final @NotNull CombatServerPlugin plugin;

    public KitCommand(@NotNull CombatServerPlugin plugin) {
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
        if (!sender.hasPermission(Permissions.CREATE_KIT_COMMAND)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfiguration().getKitCommandHelp());
            return true;
        }

        // Serialize kits' content and save it
        String kitName = args[0];
        PlayerInventory playerInventory = player.getInventory();
        InventoryContent inventoryContent = new InventoryContent(kitName, playerInventory.getContents());
        plugin.getDatabase().saveContent(inventoryContent);

        ArmorStand armorStand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        armorStand.setArms(true);
        armorStand.setItem(EquipmentSlot.HAND, playerInventory.getItemInMainHand());
        armorStand.setItem(EquipmentSlot.OFF_HAND, playerInventory.getItemInOffHand());
        armorStand.setItem(EquipmentSlot.HEAD, playerInventory.getHelmet());
        armorStand.setItem(EquipmentSlot.CHEST, playerInventory.getChestplate());
        armorStand.setItem(EquipmentSlot.LEGS, playerInventory.getLeggings());
        armorStand.setItem(EquipmentSlot.FEET, playerInventory.getBoots());
        armorStand.customName(Component.text(kitName));
        armorStand.setCustomNameVisible(true);
        return true;
    }
}
