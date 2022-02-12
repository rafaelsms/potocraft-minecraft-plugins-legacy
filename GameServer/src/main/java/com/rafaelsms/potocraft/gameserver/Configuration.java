package com.rafaelsms.potocraft.gameserver;

import com.rafaelsms.potocraft.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class Configuration extends YamlFile {

    public Configuration(@NotNull GameServerPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public Set<String> getUhcWorldNames() {
        return Set.copyOf(Objects.requireNonNull(get("configuration.uhc.worlds")));
    }
}
