package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.BlockSearch;
import com.rafaelsms.potocraft.serverutility.util.FastBreakStorage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import java.util.function.Predicate;

public class QuickBreakOreListener implements Listener {

    private static final int MAX_BLOCKS_SELECTED = 24;
    private static final int MAX_BREAKS_PER_TICK = 1;
    private static final int BREAK_PERIOD = 2;

    private final Set<Predicate<Material>> oreMaterials = Set.of(TagPredictor.of(Tag.COAL_ORES),
                                                                 TagPredictor.of(Tag.COPPER_ORES),
                                                                 TagPredictor.of(Tag.IRON_ORES),
                                                                 TagPredictor.of(Tag.GOLD_ORES),
                                                                 TagPredictor.of(Tag.LAPIS_ORES),
                                                                 TagPredictor.of(Tag.EMERALD_ORES),
                                                                 TagPredictor.of(Tag.REDSTONE_ORES),
                                                                 TagPredictor.of(Tag.DIAMOND_ORES),
                                                                 MaterialPredictor.of(Material.NETHER_QUARTZ_ORE),
                                                                 MaterialPredictor.of(Material.GLOWSTONE),
                                                                 MaterialPredictor.of(Material.ANCIENT_DEBRIS));

    private final @NotNull ServerUtilityPlugin plugin;
    private final FastBreakStorage fastBreakStorage;

    public QuickBreakOreListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
        this.fastBreakStorage = FastBreakStorage.build(plugin, null, MAX_BREAKS_PER_TICK);
    }

    @EventHandler
    private void registerTasks(ServerLoadEvent event) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, fastBreakStorage::tickBreak, BREAK_PERIOD, BREAK_PERIOD);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void quickBreakOreVein(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (player.isSneaking()) {
            return;
        }
        if (!player.hasPermission(Permissions.QUICK_BREAK_ORES)) {
            return;
        }
        Block block = event.getBlock();
        Optional<Predicate<Material>> predicateOptional = getMatchingPredicate(block.getType());
        if (predicateOptional.isEmpty()) {
            return;
        }
        Map<Location, Block> oresOptional = getOres(block, predicateOptional.get());
        if (oresOptional.isEmpty()) {
            return;
        }
        fastBreakStorage.addBlocks(player, oresOptional);
    }

    private @NotNull Map<Location, Block> getOres(@NotNull Block block,
                                                  @NotNull Predicate<Material> materialPredicate) {
        return BlockSearch.searchBlocks(block, block_ -> materialPredicate.test(block_.getType()), MAX_BLOCKS_SELECTED);
    }

    private Optional<Predicate<Material>> getMatchingPredicate(@NotNull Material material) {
        for (Predicate<Material> materialPredicate : oreMaterials) {
            if (materialPredicate.test(material)) {
                return Optional.of(materialPredicate);
            }
        }
        return Optional.empty();
    }

    private record TagPredictor(@NotNull Tag<Material> materialTag) implements Predicate<Material> {

        private TagPredictor(@NotNull Tag<Material> materialTag) {
            this.materialTag = materialTag;
        }

        @Override
        public boolean test(Material material) {
            return materialTag.isTagged(material);
        }

        public static TagPredictor of(@NotNull Tag<Material> materialTag) {
            return new TagPredictor(materialTag);
        }
    }

    private record MaterialPredictor(@NotNull Material material) implements Predicate<Material> {

        private MaterialPredictor(@NotNull Material material) {
            this.material = material;
        }

        @Override
        public boolean test(Material material) {
            return material == this.material;
        }

        public static MaterialPredictor of(@NotNull Material material) {
            return new MaterialPredictor(material);
        }
    }
}
