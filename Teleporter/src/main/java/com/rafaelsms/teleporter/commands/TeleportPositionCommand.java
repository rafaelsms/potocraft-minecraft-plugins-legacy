package com.rafaelsms.teleporter.commands;

import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Optional;

public class TeleportPositionCommand extends BaseTeleportCommand {

    public TeleportPositionCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments) {
        if (arguments.length < 3 || arguments.length > 4) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportPositionCommandHelp());
            return;
        }

        Optional<Location> locationOptional;
        if (arguments.length == 4) {
            Optional<World> worldOptional = Optional.ofNullable(plugin.getServer().getWorld(arguments[0]));
            if (worldOptional.isEmpty()) {
                user.getPlayer().sendMessage(plugin.getConfiguration().getWorldNotFoundError());
                return;
            }
            locationOptional = parseLocation(worldOptional.get(), arguments[1], arguments[2], arguments[3]);
        } else {
            locationOptional = parseLocation(user.getPlayer().getWorld(), arguments[0], arguments[1], arguments[2]);
        }

        if (locationOptional.isEmpty()) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportPositionCommandParsingError());
            return;
        }

        executeTeleportToLocation(user, user, locationOptional.get());
    }

    private Optional<Location> parseLocation(@NotNull World world,
                                             @NotNull String x,
                                             @NotNull String y,
                                             @NotNull String z) {
        try {
            return Optional.of(new Location(world,
                                            parseToNumber(x).orElseThrow(),
                                            parseToNumber(y).orElseThrow(),
                                            parseToNumber(z).orElseThrow()));
        } catch (NoSuchElementException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Double> parseToNumber(@NotNull String coordinate) {
        try {
            return Optional.of(Double.valueOf(coordinate));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
