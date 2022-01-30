package com.rafaelsms.potocraft.blockprotection.util;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectionBox extends LocationBox {

    protected SelectionBox(@NotNull BlockProtectionPlugin plugin, @NotNull Location location) {
        super(getSelectionMin(plugin, location), getSelectionMax(plugin, location));
    }

    protected SelectionBox(@NotNull BlockProtectionPlugin plugin,
                           @NotNull SelectionBox otherBox,
                           @NotNull Location expansionLocation) {
        super(expandLowerCorner(getSelectionMin(plugin, expansionLocation), otherBox.getLowerCorner()),
              expandHigherCorner(getSelectionMax(plugin, expansionLocation), otherBox.getHigherCorner()));
    }

    public static SelectionBox makeSelection(@NotNull BlockProtectionPlugin plugin,
                                             @Nullable SelectionBox currentSelection,
                                             @NotNull Location selectedLocation) {
        if (currentSelection != null) {
            return new SelectionBox(plugin, currentSelection, selectedLocation);
        } else {
            return new SelectionBox(plugin, selectedLocation);
        }
    }

    private static Location getSelectionMin(@NotNull BlockProtectionPlugin plugin, @NotNull Location location) {
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        int minYOffset = plugin.getConfiguration().getSelectionMinYOffset();
        return location.clone().subtract(xzOffset, minYOffset, xzOffset);
    }

    private static Location getSelectionMax(@NotNull BlockProtectionPlugin plugin, @NotNull Location location) {
        int xzOffset = plugin.getConfiguration().getSelectionXZOffset();
        int maxYOffset = plugin.getConfiguration().getSelectionMaxYOffset();
        return location.clone().add(xzOffset, maxYOffset, xzOffset);
    }

    private static Location expandLowerCorner(@NotNull Location location1, @NotNull Location location2) {
        if (!location1.getWorld().getUID().equals(location2.getWorld().getUID())) {
            throw new IllegalArgumentException("Can't expand selection from different worlds!");
        }
        return new Location(location1.getWorld(),
                            Math.min(location1.getBlockX(), location2.getBlockX()),
                            Math.min(location1.getBlockY(), location2.getBlockY()),
                            Math.min(location1.getBlockZ(), location2.getBlockZ()));
    }

    private static Location expandHigherCorner(@NotNull Location location1, @NotNull Location location2) {
        if (!location1.getWorld().getUID().equals(location2.getWorld().getUID())) {
            throw new IllegalArgumentException("Can't expand selection from different worlds!");
        }
        return new Location(location1.getWorld(),
                            Math.max(location1.getBlockX(), location2.getBlockX()),
                            Math.max(location1.getBlockY(), location2.getBlockY()),
                            Math.max(location1.getBlockZ(), location2.getBlockZ()));
    }
}
