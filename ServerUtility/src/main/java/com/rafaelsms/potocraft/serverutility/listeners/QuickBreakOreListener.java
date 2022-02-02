package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.BlockSearch;
import com.rafaelsms.potocraft.serverutility.util.FastBreakStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class QuickBreakOreListener implements Listener {

    private final Set<Material> oreMaterials = Set.of(Material.COAL,
                                                      Material.DEEPSLATE_COAL_ORE,
                                                      Material.COPPER_ORE,
                                                      Material.DEEPSLATE_COPPER_ORE,
                                                      Material.IRON_ORE,
                                                      Material.DEEPSLATE_IRON_ORE,
                                                      Material.GOLD_ORE,
                                                      Material.DEEPSLATE_GOLD_ORE,
                                                      Material.NETHER_GOLD_ORE,
                                                      Material.LAPIS_ORE,
                                                      Material.DEEPSLATE_LAPIS_ORE,
                                                      Material.EMERALD_ORE,
                                                      Material.DEEPSLATE_EMERALD_ORE,
                                                      Material.REDSTONE_ORE,
                                                      Material.DEEPSLATE_REDSTONE_ORE,
                                                      Material.DIAMOND_ORE,
                                                      Material.DEEPSLATE_DIAMOND_ORE,
                                                      Material.NETHER_QUARTZ_ORE,
                                                      Material.ANCIENT_DEBRIS);

    private final @NotNull ServerUtilityPlugin plugin;
    private final FastBreakStorage fastBreakStorage = new FastBreakStorage();

    public QuickBreakOreListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void registerTasks(ServerLoadEvent event) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, fastBreakStorage::tickBreak, 1, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void quickBreakOreVein(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(Permissions.QUICK_BREAK_ORES)) {
            return;
        }
        Block block = event.getBlock();
        if (!oreMaterials.contains(block.getType())) {
            return;
        }
        if (fastBreakStorage.containsBlock(block)) {
            return;
        }
        Optional<Map<Location, Block>> oresOptional = getOres(block);
        if (oresOptional.isEmpty()) {
            return;
        }
        fastBreakStorage.addBlocks(player, oresOptional.get());
    }

    private @NotNull Optional<Map<Location, Block>> getOres(@NotNull Block block) {
        Material type = block.getType();
        return Optional.of(BlockSearch.searchBlocks(block, block_ -> block_.getType() == type));
    }
}
