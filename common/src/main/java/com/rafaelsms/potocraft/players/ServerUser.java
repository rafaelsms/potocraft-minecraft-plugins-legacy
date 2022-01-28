package com.rafaelsms.potocraft.players;

import com.rafaelsms.potocraft.database.pojo.ServerProfile;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ServerUser extends User<Player, ServerPlayer, ServerProfile> {

    public ServerUser(@NotNull Player player, @NotNull ServerProfile playerObject) {
        super(new ServerPlayer(player), playerObject);
    }
}
