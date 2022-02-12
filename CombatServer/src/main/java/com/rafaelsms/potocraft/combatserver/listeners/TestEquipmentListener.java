package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestEquipmentListener implements Listener {

    private final @NotNull CombatServerPlugin plugin;

    private final HashMap<Armor, Double> damageDone = new HashMap<>();

    private final List<Material> helmets = List.of(Material.AIR,
                                                   Material.LEATHER_HELMET,
                                                   Material.CHAINMAIL_HELMET,
                                                   Material.GOLDEN_HELMET,
                                                   Material.IRON_HELMET,
                                                   Material.IRON_HELMET,
                                                   Material.DIAMOND_HELMET,
                                                   Material.NETHERITE_HELMET);
    private final List<Material> chestplates = List.of(Material.AIR,
                                                       Material.LEATHER_CHESTPLATE,
                                                       Material.CHAINMAIL_CHESTPLATE,
                                                       Material.GOLDEN_CHESTPLATE,
                                                       Material.IRON_CHESTPLATE,
                                                       Material.DIAMOND_CHESTPLATE,
                                                       Material.DIAMOND_CHESTPLATE,
                                                       Material.NETHERITE_CHESTPLATE);
    private final List<Material> leggings = List.of(Material.AIR,
                                                    Material.LEATHER_LEGGINGS,
                                                    Material.CHAINMAIL_LEGGINGS,
                                                    Material.GOLDEN_LEGGINGS,
                                                    Material.IRON_LEGGINGS,
                                                    Material.DIAMOND_LEGGINGS,
                                                    Material.DIAMOND_LEGGINGS,
                                                    Material.NETHERITE_LEGGINGS);
    private final List<Material> boots = List.of(Material.AIR,
                                                 Material.LEATHER_BOOTS,
                                                 Material.CHAINMAIL_BOOTS,
                                                 Material.GOLDEN_BOOTS,
                                                 Material.IRON_BOOTS,
                                                 Material.IRON_BOOTS,
                                                 Material.DIAMOND_BOOTS,
                                                 Material.NETHERITE_BOOTS);

    public TestEquipmentListener(@NotNull CombatServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        int delay = 20 * 10;
        List<ItemStack> helmets = getAllMaterials(this.helmets);
        List<ItemStack> chestplates = getAllMaterials(this.chestplates);
        List<ItemStack> leggings = getAllMaterials(this.leggings);
        List<ItemStack> boots = getAllMaterials(this.boots);

        for (AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers()) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(modifier);
        }
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)
              .addModifier(new AttributeModifier(UUID.randomUUID(),
                                                 "attack-damage-test",
                                                 10.0,
                                                 AttributeModifier.Operation.ADD_NUMBER));
        for (int i = 0; i < helmets.size(); i++) {
            Armor armor = new Armor(helmets.get(i), chestplates.get(i), leggings.get(i), boots.get(i));
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                armor.apply(player);
                double initialHealth = player.getHealth();
                player.setNoDamageTicks(0);
                player.setInvulnerable(false);
                player.attack(player);
                double finalHealth = player.getHealth();
                player.setHealth(initialHealth);
                damageDone.put(armor, initialHealth - finalHealth);
            }, delay);
            delay++;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            damageDone.entrySet()
                      .stream()
                      .sorted(Map.Entry.comparingByValue())
                      .forEach(armorDoubleEntry -> plugin.logger()
                                                         .info("{} -> {}",
                                                               armorDoubleEntry.getKey().toString(),
                                                               armorDoubleEntry.getValue()));
            event.getPlayer().kick(Component.text("done testing, results in log"));
        }, delay + 10);
    }

    private List<ItemStack> getAllMaterials(List<Material> materials) {
        List<ItemStack> items = new LinkedList<>();
        for (Material material : materials) {
            items.addAll(getAllProtections(material));
        }
        return items;
    }

    private List<ItemStack> getAllProtections(Material material) {
        List<ItemStack> items = new LinkedList<>();
        if (Enchantment.PROTECTION_ENVIRONMENTAL.canEnchantItem(new ItemStack(material))) {
            for (int i = 0; i <= Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel(); i++) {
                ItemStack item = new ItemStack(material);
                if (i > 0) {
                    item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, i);
                }
                items.add(item);
            }
        } else {
            items.add(new ItemStack(material));
        }
        return items;
    }

    private record Armor(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {

        private ItemStack getOrNull(ItemStack itemStack) {
            if (itemStack.getType() == Material.AIR) {
                return null;
            }
            return itemStack;
        }

        public void apply(Player player) {
            player.getInventory().setItem(EquipmentSlot.HEAD, getOrNull(helmet));
            player.getInventory().setItem(EquipmentSlot.CHEST, getOrNull(chestplate));
            player.getInventory().setItem(EquipmentSlot.LEGS, getOrNull(leggings));
            player.getInventory().setItem(EquipmentSlot.FEET, getOrNull(boots));
        }

        private String formatItem(ItemStack itemStack) {
            if (!itemStack.hasEnchants()) {
                return itemStack.getType().name();
            }
            StringBuilder stringBuilder = new StringBuilder(itemStack.getType().name()).append(" (");
            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
                stringBuilder.append(entry.getKey().getKey().getKey())
                             .append(' ')
                             .append(entry.getValue())
                             .append(", ");
            }
            stringBuilder.append(")");
            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return "Armor{" +
                   "helmet=" +
                   formatItem(helmet) +
                   ", chestplate=" +
                   formatItem(chestplate) +
                   ", leggings=" +
                   formatItem(leggings) +
                   ", boots=" +
                   formatItem(boots) +
                   '}';
        }
    }
}
