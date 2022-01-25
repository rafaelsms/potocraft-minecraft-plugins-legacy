package com.rafaelsms.potocraft.blockprotection.players;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class User {

    private final @NotNull BlockProtectionPlugin plugin;
    private final @NotNull Player player;

    private final @NotNull BlockChangeCache blockChangeCache;
    private @Nullable Selection selection = null;

    public User(@NotNull BlockProtectionPlugin plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.blockChangeCache = new BlockChangeCache(plugin, this);
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull UUID getPlayerId() {
        return player.getUniqueId();
    }

    public void addSelection(@NotNull Location blockLocation) {
        if (this.selection == null) {
            this.selection = new Selection(blockLocation);
        } else {
            this.selection.select(blockLocation);
        }
        this.blockChangeCache.showRectangle(selection.getCorner1(),
                                            selection.getCorner2(),
                                            Material.GLOWSTONE.createBlockData());
    }

    public void clearSelection() {
        this.selection = null;
        this.blockChangeCache.clearBlocks();
    }

    public Optional<Selection> getSelection() {
        return Optional.ofNullable(selection);
    }

    private class Selection {

        private @NotNull World world;
        private @NotNull Location corner1;
        private @NotNull Location corner2;

        private Selection(@NotNull Location location) {
            this.world = location.getWorld();
            this.corner1 = getSelectionMin(location);
            this.corner2 = getSelectionMax(location);
        }

        public @NotNull Location getCorner1() {
            return corner1;
        }

        public @NotNull Location getCorner2() {
            return corner2;
        }

        private Location getSelectionMin(@NotNull Location location) {
            int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
            int minYOffset = plugin.getConfiguration().getSelectionMinYOffset();
            return location.clone().subtract(xzOffset, minYOffset, xzOffset);
        }

        private Location getSelectionMax(@NotNull Location location) {
            int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
            int maxYOffset = plugin.getConfiguration().getSelectionMaxYOffset();
            return location.clone().add(xzOffset, maxYOffset, xzOffset);
        }

        private void select(@NotNull Location location) {
            if (!location.getWorld().getUID().equals(world.getUID())) {
                throw new IllegalStateException("Different worlds on selection!");
            }
            Location selection1 = getSelectionMin(location);
            Location selection2 = getSelectionMax(location);
            this.corner1 = new Location(world,
                                        Math.min(selection1.getBlockX(), corner1.getBlockX()),
                                        Math.min(selection1.getBlockY(), corner1.getBlockY()),
                                        Math.min(selection1.getBlockZ(), corner1.getBlockZ()));
            this.corner2 = new Location(world,
                                        Math.max(selection2.getBlockX(), corner2.getBlockX()),
                                        Math.max(selection2.getBlockY(), corner2.getBlockY()),
                                        Math.max(selection2.getBlockZ(), corner2.getBlockZ()));
        }

    }
}
