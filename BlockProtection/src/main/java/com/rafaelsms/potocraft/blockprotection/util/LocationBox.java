package com.rafaelsms.potocraft.blockprotection.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationBox extends Box {

    protected final @NotNull Location lowerCorner;
    protected final @NotNull Location higherCorner;

    public LocationBox(@NotNull Location lowerCorner, @NotNull Location higherCorner) {
        super(lowerCorner, higherCorner);
        this.lowerCorner = lowerCorner;
        this.higherCorner = higherCorner;
    }

    public @NotNull Location getHigherCorner() {
        return higherCorner;
    }

    public @NotNull Location getLowerCorner() {
        return lowerCorner;
    }
}
