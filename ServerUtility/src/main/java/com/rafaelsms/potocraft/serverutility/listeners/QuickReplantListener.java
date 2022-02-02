package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.BlockSearch;
import com.rafaelsms.potocraft.serverutility.util.FastBreakStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class QuickReplantListener implements Listener {

    private final Set<Crop> cropMaterials = Set.of(Crop.of(Material.CARROTS, Material.CARROT),
                                                   Crop.of(Material.POTATOES, Material.POTATO),
                                                   Crop.of(Material.WHEAT, Material.WHEAT_SEEDS),
                                                   Crop.of(Material.NETHER_WART));

    private final @NotNull ServerUtilityPlugin plugin;
    private final FastBreakStorage fastBreakStorage = new FastBreakStorage(new ReplantTask());
    private final HashMap<Location, Crop> storedCropType = new HashMap<>();

    public QuickReplantListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void registerTasks(ServerLoadEvent event) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            fastBreakStorage.tickBreak();
            // Prevent any "memory leak" with this bad design
            if (fastBreakStorage.isEmpty()) {
                storedCropType.clear();
            }
        }, 1, 1);
    }

    @EventHandler(ignoreCancelled = true)
    private void removeOneSeedFromDrop(@NotNull BlockDropItemEvent event) {
        Block block = event.getBlock();
        Optional<Crop> optionalCrop = getCrop(block);
        if (optionalCrop.isEmpty()) {
            return;
        }
        Crop crop = optionalCrop.get();
        Crop storedCrop = storedCropType.get(block.getLocation());
        if (!Objects.equals(storedCrop, crop)) {
            return;
        }

        Iterator<Item> iterator = event.getItems().iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            ItemStack itemStack = item.getItemStack();

            if (crop.isSeed(itemStack)) {
                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    item.setItemStack(itemStack);
                } else {
                    iterator.remove();
                }
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void replantCrop(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            return;
        }
        if (!player.hasPermission(Permissions.QUICK_BREAK_CROP)) {
            return;
        }
        Block block = event.getBlock();
        if (fastBreakStorage.containsBlock(block)) {
            return;
        }
        Optional<Crop> optionalCrop = getCrop(block);
        if (optionalCrop.isEmpty()) {
            return;
        }
        Crop crop = optionalCrop.get();
        Optional<Map<Location, Block>> cropsOptional = getSameGrownCrop(block, crop);
        if (cropsOptional.isEmpty()) {
            return;
        }
        for (Location location : cropsOptional.get().keySet()) {
            storedCropType.put(location, crop);
        }
        fastBreakStorage.addBlocks(player, cropsOptional.get());
    }

    private Optional<Crop> getCrop(@NotNull Block block) {
        Crop crop = null;
        for (Crop cropMaterial : cropMaterials) {
            if (cropMaterial.isDoneCrop(block)) {
                crop = cropMaterial;
                break;
            }
        }
        return Optional.ofNullable(crop);
    }

    private @NotNull Optional<Map<Location, Block>> getSameGrownCrop(@NotNull Block block, @NotNull Crop crop) {
        return Optional.of(BlockSearch.searchBlocks(block, crop::isDoneCrop));
    }

    private class ReplantTask implements Consumer<Block> {

        @Override
        public void accept(Block block) {
            Crop crop = storedCropType.remove(block.getLocation());
            if (crop == null) {
                return;
            }
            Ageable ageable = (Ageable) crop.getBlockData();
            ageable.setAge(0);
            block.setBlockData(ageable, true);
        }
    }

    private record Crop(Material crop, Material seed) {

        public static Crop of(@NotNull Material crop, @NotNull Material seed) {
            return new Crop(crop, seed);
        }

        public static Crop of(@NotNull Material cropSeed) {
            return new Crop(cropSeed, cropSeed);
        }

        public BlockData getBlockData() {
            return crop.createBlockData();
        }

        public boolean isDoneCrop(@NotNull Block block) {
            if (block.getType() != crop) {
                return false;
            }
            Ageable ageable = (Ageable) block.getBlockData();
            return ageable.getAge() == ageable.getMaximumAge();
        }

        public boolean isSeed(@NotNull ItemStack itemStack) {
            return itemStack.getType() == seed;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return false;
            }
            if (object == null || object.getClass() != getClass()) {
                return false;
            }
            Crop otherCrop = (Crop) object;
            return crop == otherCrop.crop && seed == otherCrop.seed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(crop, seed);
        }
    }
}
