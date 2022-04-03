package com.rafaelsms.teleporter.player;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class Home implements Comparable<Home> {

    private final @NotNull Location location;
    private final @NotNull ZonedDateTime creationDate;

    public Home(@NotNull Location location, @NotNull ZonedDateTime creationDate) {
        this.location = location;
        this.creationDate = creationDate;
    }

    public Home(@NotNull Location location) {
        this(location, ZonedDateTime.now());
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public @NotNull ZonedDateTime getCreationDate() {
        return creationDate;
    }

    @Override
    public int compareTo(@NotNull Home o) {
        return creationDate.compareTo(o.creationDate);
    }
}
